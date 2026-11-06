export interface EmailMessage {
  from: string;
  to: string;
  replyTo?: string;
  subject: string;
  /** Plain text only: avoids any HTML injection from customer-supplied fields. */
  text: string;
}

/**
 * Boundary for outbound email. SMTP today; swap in SES, SendGrid, etc. by
 * implementing this interface.
 */
export interface EmailSender {
  send(message: EmailMessage): Promise<void>;
}

export class EmailDeliveryError extends Error {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, options);
    this.name = "EmailDeliveryError";
  }
}
