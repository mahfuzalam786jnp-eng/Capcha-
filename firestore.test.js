const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(uid, now) {
  return {
    userId: uid,
    fullName: "Aarav Sharma",
    mobileNumber: "9876543210",
    email: "aarav@example.com",
    referralCode: "DP9X4K2M",
    referredByCode: "",
    walletBalance: 0,
    totalDeposit: 0,
    totalWithdrawal: 0,
    dailyStreakCount: 0,
    lastCheckInEpochDay: 0,
    passwordHash: "Y29tcGxleF9wYmtkZjJfaGFzaF8xMjM0NTY=",
    passwordSalt: "cmFuZG9tX3NhbHRfMTIzNDU2",
    languagePref: "EN",
    createdAt: now,
    updatedAt: now,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any Desi Paisa collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, now))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("deposits").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get()
  );
});

test("Owner can create valid profile with zero initial balance; rejects inflated balance or ghost fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 2000);

  // Reject inflated initial balance
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .set({ ...validProfilePayload(ALICE_UID, now), walletBalance: 5000 })
  );

  // Reject ghost field (Shadow Update test)
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .set({ ...validProfilePayload(ALICE_UID, now), isAdmin: true })
  );

  // Reject @DocumentId "id" stored in document body
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .set({ ...validProfilePayload(ALICE_UID, now), id: ALICE_UID })
  );

  // Valid create succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, now))
  );
});

test("Cross-user isolation blocks Bob from reading or modifying Alice's profile and subcollections", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, now))
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .update({ fullName: "Hijacked Name", updatedAt: new Date(Date.now() - 500) })
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("deposits").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("withdrawals").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("support_tickets").get()
  );
});

test("Profile actions: edit profile, change password, and daily check-in work while blocking arbitrary balance tampering", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(profileRef.set(validProfilePayload(ALICE_UID, created)));

  // Edit profile + language
  await assertSucceeds(
    profileRef.update({
      fullName: "Aarav Verma",
      mobileNumber: "9123456789",
      languagePref: "HI",
      updatedAt: new Date(Date.now() - 5000),
    })
  );

  // Change password hash + salt
  await assertSucceeds(
    profileRef.update({
      passwordHash: "bmV3X3Bia2RmMl9oYXNoXzY1NDMyMQ==",
      passwordSalt: "bmV3X3NhbHRfNjU0MzIx",
      updatedAt: new Date(Date.now() - 4000),
    })
  );

  // Daily check-in (+25 demo bonus, epoch day > 0)
  await assertSucceeds(
    profileRef.update({
      walletBalance: 25,
      dailyStreakCount: 1,
      lastCheckInEpochDay: 20775,
      updatedAt: new Date(Date.now() - 3000),
    })
  );

  // Duplicate check-in on same epoch day fails
  await assertFails(
    profileRef.update({
      walletBalance: 50,
      dailyStreakCount: 2,
      lastCheckInEpochDay: 20775,
      updatedAt: new Date(Date.now() - 2000),
    })
  );

  // Tampering referralCode or createdAt fails
  await assertFails(
    profileRef.update({
      referralCode: "HACKED99",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Deposit & Withdrawal workflow: enforces PENDING creation, terminal state locking, and idempotencyKey == docId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const depId = "dep_req_001";
  const depRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("deposits")
    .doc(depId);

  // Cannot create pre-approved deposit
  await assertFails(
    depRef.set({
      userId: ALICE_UID,
      idempotencyKey: depId,
      amount: 500,
      paymentMethod: "DEMO_UPI",
      referenceNote: "Test",
      status: "APPROVED",
      demoMode: true,
      adminRemark: "",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Valid PENDING deposit succeeds
  await assertSucceeds(
    depRef.set({
      userId: ALICE_UID,
      idempotencyKey: depId,
      amount: 500,
      paymentMethod: "DEMO_UPI",
      referenceNote: "Demo UPI top-up",
      status: "PENDING",
      demoMode: true,
      adminRemark: "",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Transition PENDING -> APPROVED succeeds
  await assertSucceeds(
    depRef.update({
      status: "APPROVED",
      adminRemark: "Verified in demo sandbox",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Terminal state lock: cannot update APPROVED deposit again
  await assertFails(
    depRef.update({
      status: "REJECTED",
      adminRemark: "Attempted override",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Withdrawal: valid PENDING -> REJECTED -> locked
  const wdId = "wd_req_001";
  const wdRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("withdrawals")
    .doc(wdId);

  await assertSucceeds(
    wdRef.set({
      userId: ALICE_UID,
      idempotencyKey: wdId,
      amount: 200,
      payoutMethod: "UPI",
      payoutDestination: "aarav@demo-upi",
      accountHolderName: "Aarav Sharma",
      status: "PENDING",
      demoMode: true,
      adminRemark: "",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    wdRef.update({
      status: "REJECTED",
      adminRemark: "Demo rejection test",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertFails(
    wdRef.update({
      status: "APPROVED",
      adminRemark: "Re-approve attempt",
      updatedAt: new Date(Date.now() - 200),
    })
  );
});

test("Transactions, Referrals & Support Tickets: scoped list queries succeed and immutable deletes fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 2000);

  const txRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("transactions")
    .doc("tx_001_demo");

  await assertSucceeds(
    txRef.set({
      userId: ALICE_UID,
      idempotencyKey: "tx_001_demo",
      type: "DAILY_ACTIVITY",
      title: "Daily Check-in Bonus",
      titleHi: "दैनिक चेक-इन बोनस",
      amount: 25,
      direction: "CREDIT",
      status: "COMPLETED",
      demoMode: true,
      relatedRequestId: "",
      createdAt: now,
    })
  );

  // Delete transaction is forbidden
  await assertFails(txRef.delete());

  // Referral create & list
  const refDoc = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("referrals")
    .doc("ref_001");
  await assertSucceeds(
    refDoc.set({
      userId: ALICE_UID,
      inviteeName: "Priya Patel",
      inviteeCodeOrPhone: "98****2109",
      status: "VERIFIED",
      rewardPoints: 50,
      demoMode: true,
      createdAt: now,
    })
  );

  // Support ticket create & list
  const ticketDoc = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("support_tickets")
    .doc("tkt_001");
  await assertSucceeds(
    ticketDoc.set({
      userId: ALICE_UID,
      category: "GENERAL",
      subject: "Need help with demo deposit",
      message: "Testing support ticket creation in Desi Paisa.",
      status: "OPEN",
      createdAt: now,
    })
  );

  // Future timestamp rejected
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("support_tickets")
      .doc("tkt_future")
      .set({
        userId: ALICE_UID,
        category: "GENERAL",
        subject: "Future",
        message: "Tampered future timestamp",
        status: "OPEN",
        createdAt: new Date(Date.now() + 120000),
      })
  );

  // List queries succeed on owner subcollections
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("referrals").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("support_tickets").get()
  );
});
