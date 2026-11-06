import type { EmailMessage, EmailSender } from "./emailSender";

/** Dev fallback used when no SMTP host is configured: prints instead of sending. */
export class LogEmailSender implements EmailSender {
  async send(message: EmailMessage): Promise<void> {
    console.log(
      `[email not sent: SMTP_HOST unset]\nTo: ${message.to}\nSubject: ${message.subject}\n\n${message.text}\n`,
    );
  }
}
