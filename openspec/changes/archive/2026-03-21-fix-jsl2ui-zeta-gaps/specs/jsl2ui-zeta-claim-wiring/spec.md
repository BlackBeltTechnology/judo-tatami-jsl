## ADDED Requirements

### Requirement: Zeta ClaimModifier rule sets attributeType
The Zeta `claimModifier()` rule in `ModifiableRules.java` SHALL set the `attributeType` reference on the created `Claim` object to the `AttributeType` equivalent of the actor's identity field, matching the ETL `ClaimModifier` rule behavior.

#### Scenario: Actor with realm and claim identity produces valid Claim
- **WHEN** a JSL model defines an actor with `realm`, `claim`, and `identity` pointing to a transfer field (e.g., `actor UserActor realm: "COMPANY" claim: "email" identity: UserTransfer::email`)
- **THEN** the Zeta transformation SHALL produce a `Claim` object whose `attributeType` reference points to the `AttributeType` created for the identity's transfer field declaration

#### Scenario: Claim attributeType resolves for mapped transfer fields
- **WHEN** the identity field is a mapped transfer field (uses `<=>` binding)
- **THEN** the `attributeType` SHALL resolve via `Jsl2UiHelper.getTransferFieldAttributeType()` using the `CREATE_MAPPED_TRANSFER_ATTRIBUTE` rule name

#### Scenario: UI model validation passes for actors with claims
- **WHEN** the Zeta transformation completes for a model containing actors with realm/claim/identity
- **THEN** `uiModel.isValid()` SHALL return `true` with no "required feature 'attributeType' must be set" diagnostics
