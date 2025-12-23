package com.example.rental.entity;

/**
 * Các hành động được ghi nhận trong Audit Log
 */
public enum AuditAction {
    // Authentication
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    REGISTER_GUEST,
    REGISTER_TENANT,
    REGISTER_PARTNER,
    REGISTER_EMPLOYEE,

    // Contract actions
    CREATE_CONTRACT,
    UPDATE_CONTRACT,
    EXTEND_CONTRACT,
    TERMINATE_CONTRACT,
    SIGN_CONTRACT,

    // Invoice actions
    CREATE_INVOICE,
    UPDATE_INVOICE,
    CONFIRM_PAYMENT,
    REJECT_PAYMENT,
    CANCEL_INVOICE,

    // Price/Service changes
    UPDATE_PRICE,
    ADD_SERVICE,
    REMOVE_SERVICE,

    // Tenant/User management
    CREATE_TENANT,
    UPDATE_TENANT,
    BAN_TENANT,
    UNBAN_TENANT,
    CREATE_EMPLOYEE,
    UPDATE_EMPLOYEE,

    // Partner management
    CREATE_PARTNER_POST,
    APPROVE_PARTNER_POST,
    REJECT_PARTNER_POST,
    UPDATE_PARTNER_POST,
    DELETE_PARTNER_POST,

    // Room management
    CREATE_ROOM,
    UPDATE_ROOM,
    CHANGE_ROOM_STATUS,
    DELETE_ROOM,

    // Reservation (booking) actions
    CREATE_RESERVATION,
    CONFIRM_RESERVATION,
    CANCEL_RESERVATION,

    // Maintenance
    CREATE_MAINTENANCE_REQUEST,
    UPDATE_MAINTENANCE_STATUS,
    COMPLETE_MAINTENANCE,

    // Permission changes
    ASSIGN_ROLE,
    REMOVE_ROLE,
    GRANT_PERMISSION,
    REVOKE_PERMISSION,

    // System operations
    MANUAL_ADJUSTMENT,
    SYSTEM_AUTO_ACTION,
    BACKUP_DATA,
    DELETE_DATA,

    // Checkout process
    SUBMIT_CHECKOUT_REQUEST,
    APPROVE_CHECKOUT,
    DAMAGE_ASSESSMENT,
    FINAL_SETTLEMENT
}
