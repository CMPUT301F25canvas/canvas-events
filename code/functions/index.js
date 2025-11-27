const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

/**
 * Sends an FCM push whenever a notification record is created in Firestore.
 * Watches the same collection tree the app writes to: notifications/{templateId}/events/{eventId}/recipients/{doc}
 */
exports.pushNotificationOnCreate = onDocumentCreated(
  "notifications/{templateId}/events/{eventId}/recipients/{recipientDoc}",
  async (event) => {
    const snap = event.data;
    if (!snap) {
      return;
    }
    const data = snap.data();
    const recipientId = data.recipientId;
    if (!recipientId) {
      logger.warn("No recipientId on notification document; skipping push.");
      return;
    }

    // Collect tokens for this recipient
    const tokensSnap = await admin
      .firestore()
      .collection("users")
      .doc(recipientId)
      .collection("fcmTokens")
      .get();
    const tokens = tokensSnap.docs.map((d) => d.id).filter(Boolean);
    if (!tokens.length) {
      logger.info(`No FCM tokens for recipient ${recipientId}; skipping push.`);
      return;
    }

    const templateId = event.params.templateId || data.templateId || "";
    const eventId = event.params.eventId || data.eventId || "";
    const title = data.title || "Organizer update";
    const body = data.body || "";

    const message = {
      tokens,
      notification: {
        title,
        body,
      },
      data: {
        templateId,
        eventId,
        recipientId,
        eventName: data.eventName || "",
        type: data.type || "",
        status: data.status || "",
        waitlistEntryId: data.waitlistEntryId || "",
        source: data.source || "",
        title,
        body,
      },
    };

    const response = await admin.messaging().sendEachForMulticast(message);
    logger.info(
      `Push sent for notification ${snap.id} (template ${templateId}, event ${eventId})`,
      { success: response.successCount, failure: response.failureCount }
    );
  }
);
