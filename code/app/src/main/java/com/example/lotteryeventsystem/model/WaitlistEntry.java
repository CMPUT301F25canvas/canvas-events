package com.example.lotteryeventsystem.model;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

/**
 * Represents a single entrant on a waitlist.
 */
public class WaitlistEntry {
    private String id;
    private String entrantId;
    private String entrantName;
    private String contactEmail;
    private String contactPhone;
    private WaitlistStatus status;
    private Timestamp joinedAt;
    private Timestamp invitedAt;

    public WaitlistEntry() {
        // Firestore reflection needs this.
    }

    public WaitlistEntry(String id,
                         String entrantId,
                         String entrantName,
                         WaitlistStatus status) {
        this.id = id;
        this.entrantId = entrantId;
        this.entrantName = entrantName;
        this.status = status;
    }

    /**
     * Gets the Firestore id for this waitlist row.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the Firestore id. Usually this is handled by Firestore.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the user id of the entrant.
     */
    public String getEntrantId() {
        return entrantId;
    }

    /**
     * Sets the user id of the entrant.
     */
    public void setEntrantId(String entrantId) {
        this.entrantId = entrantId;
    }

    /**
     * Friendly entrant display name.
     */
    public String getEntrantName() {
        return entrantName;
    }

    /**
     * Sets the entrant name we show in the UI.
     */
    public void setEntrantName(String entrantName) {
        this.entrantName = entrantName;
    }

    /**
     * Preferred contact email.
     */
    public String getContactEmail() {
        return contactEmail;
    }

    /**
     * Stores the contact email.
     */
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    /**
     * Preferred contact phone number.
     */
    public String getContactPhone() {
        return contactPhone;
    }

    /**
     * Stores the contact phone number.
     */
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    /**
     * Returns the current waitlist status.
     */
    public WaitlistStatus getStatus() {
        return status;
    }

    /**
     * Updates the waitlist status.
     */
    public void setStatus(WaitlistStatus status) {
        this.status = status;
    }

    /**
     * Timestamp for when the entrant first joined the waitlist.
     */
    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    /**
     * Sets the join time.
     */
    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }

    /**
     * Timestamp for when the entrant received an invite.
     */
    public Timestamp getInvitedAt() {
        return invitedAt;
    }

    /**
     * Sets the invite time.
     */
    public void setInvitedAt(Timestamp invitedAt) {
        this.invitedAt = invitedAt;
    }

    /**
     * Picks email or phone to show as the primary contact.
     */
    @Nullable
    public String primaryContact() {
        if (contactEmail != null && !contactEmail.isEmpty()) {
            return contactEmail;
        }
        return contactPhone;
    }
}
