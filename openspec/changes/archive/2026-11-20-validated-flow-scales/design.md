# Design

## Decisions

1. Both short forms have 9 items, one per published flow dimension, scored 1 (strongly disagree) to 5 (strongly agree), following Jackson, Martin, and Eklund (2008), Journal of Sport and Exercise Psychology, 30(5), 561–587. The UI names the dimensions. It does not reproduce the copyrighted item sentences.
2. A completion stores `shortFlow` only when all 9 answers are in 1..5. A missing or partial form leaves the 1–10 fields unchanged.
3. The dispositional form is a DataStore flag. Skip and submit both mark it asked. Onboarding shows it only when the flag is unset.
4. Day stats keep mean skill as the proxy average. The form profile is a separate list of nine means, empty when nobody answered the form.

## Risks

- Readers may treat dimension names as the official items. The KDoc and the on-screen note say they are paraphrases of the dimensions, not the published sentences.
