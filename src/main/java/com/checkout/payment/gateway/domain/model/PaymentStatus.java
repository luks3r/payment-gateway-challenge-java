package com.checkout.payment.gateway.domain.model;

public enum PaymentStatus {
  AUTHORIZED("Authorized"),
  DECLINED("Declined"),
  REJECTED("Rejected");

  private final String displayName;

  PaymentStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
