# Analytics Event Map

DreamNorth analytics uses Firebase Analytics through `AppAnalytics`.

Schema version: `2026_06_v1`

## Recommended Conversions

Mark these as key events/conversions in Firebase or GA4, then import the important ones into Google Ads:

- `dream_save_success`: activation; user saved a real dream.
- `onboarding_complete`: onboarding completion.
- `premium_purchase_success`: premium subscription purchase completed.
- `store_premium_purchase_result` where `result = success`: premium purchase from the store screen.
- `token_verification_result` where `result = verified`: token purchase was verified and awarded.

For early paid acquisition, optimize toward `dream_save_success` before optimizing toward purchases. It gives Google Ads more signal than subscriptions alone.

## Conversion Snapshots

The important completion and purchase events carry a snapshot of user state at the moment they convert. These are deliberately privacy-safe: no names, emails, dream titles, or dream text.

Snapshot fields can appear on:

- `onboarding_complete`
- `premium_purchase_success`
- `store_premium_purchase_result` when `result = success`
- `store_view`

Useful snapshot params:

- `conversion_source`: `onboarding_complete`, `onboarding_premium_purchase`, or `store_screen`.
- `surface`: `onboarding` or `store`.
- `onboarding_duration_ms` and `onboarding_duration_bucket`.
- `near_goal_primary`, `near_goal_secondary`, `far_goal`, `main_blocker`, `demo_theme`, `demo_mood`, `commitment`.
- `recall_days_per_week` and `recall_bucket`.
- `premium_entry_source`, `premium_placement`, `offering_id`, `selected_plan`, `selected_package_id`, `has_trial`.
- `dream_count`, `dream_count_bucket`, `has_saved_dream`.
- `ai_interpretation_count`, `ai_art_count`, `audio_dream_count`.
- Store conversions also include `lucid_dream_count`, `nightmare_count`, `recurring_dream_count`, `token_balance`, `token_balance_bucket`, `daily_token_streak`, and `has_generated_world`.

## Onboarding Funnel

- `onboarding_step_view`: each onboarding step appeared.
- `onboarding_step_dwell`: time spent on a step.
- `onboarding_select`: user selected an onboarding answer.
- `onboarding_cta_tap`: user tapped a step CTA.
- `review_prompt_show`: in-app review prompt moment appeared.
- `basic_mode_start`: user chose/entered guest mode.
- `onboarding_complete`: onboarding finished with `completion_mode`.

Useful params: `step`, `step_index`, `total_steps`, `field`, `value`, `label`, `duration_ms`, `completion_mode`, plus the conversion snapshot fields above.

## Premium Funnel

- `premium_placement_show`: premium offering loaded for a placement.
- `premium_page_view`: premium page/stage appeared.
- `premium_page_dwell`: time spent on a premium page.
- `premium_package_select`: user selected monthly/annual package.
- `premium_cta_tap`: user tapped a premium CTA.
- `premium_purchase_start`: checkout started.
- `premium_purchase_success`: purchase succeeded.
- `premium_purchase_cancel`: user cancelled checkout.
- `premium_purchase_pending`: purchase is pending.
- `premium_purchase_error`: purchase failed.
- `premium_already_active`: user already has premium.
- `premium_restore_tap`: restore tapped.
- `premium_restore_complete`: restore completed.
- `premium_restore_error`: restore failed.
- `premium_rescue_show`: rescue/gift page shown.
- `premium_rescue_accept`: rescue/gift page accepted.
- `premium_offering_missing`: RevenueCat offering missing or empty.

Useful params: `placement`, `source`, `offering_id`, `page`, `plan`, `package_id`, `result`, `error`, plus conversion snapshot fields on successful purchases.

## Store Funnel

- `store_view`: store opened.
- `store_page_view`: premium/token tab viewed.
- `store_page_change`: user switched tabs.
- `store_close`: store closed.
- `store_premium_offer_loaded`: store premium offering loaded.
- `store_premium_offer_missing`: store premium offering missing or empty.
- `store_premium_plan_select`: user selected monthly/annual in store.
- `store_premium_purchase_start`: premium checkout from store started.
- `store_premium_purchase_result`: premium checkout from store completed/cancelled/pending/error.
- `token_product_tap`: token pack tapped.
- `token_products_fetch_result`: RevenueCat token products fetched or failed.
- `token_purchase_start`: token checkout started.
- `token_purchase_result`: token checkout result from RevenueCat.
- `token_verification_result`: backend purchase verification result.

Useful params: `initial_page`, `page`, `action`, `plan`, `package_id`, `product_id`, `token_count`, `result`, `error`, plus conversion snapshot fields on successful premium purchases.

## User Properties

- `account_type`: `signed_out`, `anonymous`, or `registered`.
- `premium_status`: `free` or `active`.

Do not add dream text, dream titles, names, emails, or raw user-entered content to Analytics events.
