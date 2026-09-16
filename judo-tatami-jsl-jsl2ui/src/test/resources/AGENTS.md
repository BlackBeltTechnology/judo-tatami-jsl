# AGENTS.md — `judo-tatami-jsl-jsl2ui/src/test/resources`

Test resources root holding the shop reference model and the external-model discovery configuration for JSL2UI test suites.

| File | Purpose |
| --- | --- |
| `TestShop.jsl` | Reference `model shop` exercising the full JSL2UI surface: `Email` constrained string type, `OrderStatus` enum, `Customer`/`Person`/`Enterprise` inheritance with opposite `Order` relations, derived `cart` and `price` expressions, `ProductListView`/`CartView`/`ThankYouView` views, `ProductRow`/`OrderItemRow` rows, and `CustomerActor` bound to `UserTransfer::email`. |
| `external-model-tests.properties` | Maps 39 model names to `src/test/resources/model/*.jsl` paths for discovery-driven JSL2UI tests. Format `<model-name>=<path>[;<param>=<value>]*`; relative paths resolve from module root `judo-tatami-jsl-jsl2ui/`, `companions` lists co-loaded JSL files. Models without a `UIFrontendDeclaration` yield empty UI models. `-Djudo.test.discovery.basedir` overrides the base directory. |
