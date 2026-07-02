package com.ufb.auth.user_management.security;

import java.util.List;

/**
 * Renders every transactional email (verification, password reset, admin claim,
 * 2FA code) inside one shared HTML shell matching the UFB Consulting brand
 * (navy/gold/ivory, serif display heading). Table-based layout with inline
 * styles, since that is what actually renders consistently across email clients.
 */
public final class EmailTemplateBuilder {

    private static final String NAVY = "#03122E";
    private static final String GOLD = "#C9A65A";
    private static final String GOLD_DARK = "#A07C2C";
    private static final String IVORY = "#F4F0E4";
    private static final String CHAR = "#223247";
    private static final String MUTE = "#5A6B82";
    private static final String LINE = "#E5DCC3";
    private static final String DISPLAY_FONT = "Georgia, 'Times New Roman', serif";
    private static final String BODY_FONT = "'Helvetica Neue', Helvetica, Arial, sans-serif";

    private EmailTemplateBuilder() {}

    /**
     * @param heading   main heading shown under the brand band
     * @param paragraphs body copy, one paragraph per entry
     * @param code      optional large letter-spaced code (e.g. a 2FA/OTP code); null to omit
     * @param ctaText   optional button label; null to omit the button
     * @param ctaUrl    target URL for the button; required if ctaText is set
     * @param footnote  small print shown below the divider (e.g. "expires at ...")
     */
    public static String render(String heading, List<String> paragraphs, String code,
                                 String ctaText, String ctaUrl, String footnote) {
        StringBuilder body = new StringBuilder();
        for (String p : paragraphs) {
            body.append("""
                    <p style="margin:0 0 16px 0; font-family:%s; font-size:15px; line-height:1.65; color:%s;">%s</p>
                    """.formatted(BODY_FONT, CHAR, p));
        }

        // Short OTP-style codes get the large, widely-spaced treatment; longer
        // tokens (e.g. the claim token) get a smaller size so they wrap instead
        // of overflowing the card.
        boolean shortCode = code != null && code.length() <= 8;
        String codeBlock = code == null ? "" : """
                <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" style="margin:8px 0 24px 0;">
                  <tr>
                    <td style="background:%s; border:1px solid %s; border-radius:4px; padding:16px 24px;
                               font-family:'Courier New', monospace; font-size:%s; font-weight:700;
                               letter-spacing:%s; color:%s; text-align:center; word-break:break-all;">%s</td>
                  </tr>
                </table>
                """.formatted(IVORY, LINE, shortCode ? "32px" : "18px", shortCode ? "10px" : "1px", NAVY, code);

        String ctaBlock = ctaText == null ? "" : """
                <table role="presentation" cellpadding="0" cellspacing="0" style="margin:8px 0 24px 0;">
                  <tr>
                    <td style="border-radius:3px; background:%s;">
                      <a href="%s" target="_blank"
                         style="display:inline-block; padding:14px 32px; font-family:%s; font-size:14px;
                                font-weight:600; letter-spacing:1px; color:%s; text-decoration:none;">%s</a>
                    </td>
                  </tr>
                </table>
                """.formatted(GOLD, ctaUrl, BODY_FONT, NAVY, ctaText);

        String footnoteBlock = footnote == null ? "" : """
                <p style="margin:0; font-family:%s; font-size:12px; line-height:1.6; color:%s;">%s</p>
                """.formatted(BODY_FONT, MUTE, footnote);

        return """
                <!DOCTYPE html>
                <html>
                <body style="margin:0; padding:0; background:%s;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:%s; padding:32px 16px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="max-width:560px; width:100%%; background:#ffffff; border-radius:6px; overflow:hidden;">
                          <tr>
                            <td style="background:%s; padding:28px 40px;">
                              <span style="font-family:%s; font-size:22px; letter-spacing:3px; color:%s;">UFB</span>
                              <div style="font-family:%s; font-size:11px; letter-spacing:2px; text-transform:uppercase; color:%s; margin-top:4px;">Unified Finance Bridge</div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:36px 40px 12px 40px;">
                              <h1 style="margin:0 0 18px 0; font-family:%s; font-size:24px; color:%s;">%s</h1>
                              %s
                              %s
                              %s
                              <div style="height:1px; background:%s; margin:8px 0 20px 0;"></div>
                              %s
                            </td>
                          </tr>
                          <tr>
                            <td style="background:%s; padding:20px 40px; text-align:center;">
                              <div style="font-family:%s; font-style:italic; font-size:12px; color:%s;">Where Capital Meets Expertise</div>
                              <div style="font-family:%s; font-size:11px; color:%s; margin-top:6px;">&copy; UFB Consulting. All rights reserved.</div>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                IVORY, IVORY,
                NAVY, DISPLAY_FONT, GOLD, BODY_FONT, "#c7d0de",
                DISPLAY_FONT, NAVY, heading,
                body,
                codeBlock,
                ctaBlock,
                LINE,
                footnoteBlock,
                NAVY, DISPLAY_FONT, GOLD, BODY_FONT, "#c7d0de"
        );
    }
}
