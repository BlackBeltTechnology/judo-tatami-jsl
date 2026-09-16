# `PsmTestModel.java`

Builds and saves a standalone PSM fixture covering the PSM metamodel features the
workflow tests need — inheritance chains, every primitive type kind, a measured type, a
containment and a bidirectional association.

**Key exports**

- `MODEL_NAME = "test"`.
- `FILE_LOCATION = "target/test-classes/psm/test-psm.model"`.
- `createPsmModelelAndSave()` — static; builds the model and calls `savePsmModel()`.
  The method name is misspelled (`Modelel`); callers must spell it that way.

**What the fixture contains** — single `Model` named `M` holding:

- types: `string` (`StringType`, maxLength 256), `int` (`NumericType`, precision 6 scale
  0), `bool` (`BooleanType`), `object` (`CustomType`), `measuredType` (`MeasuredType`,
  store unit `u`, precision 5 scale 3) and measure `measure` owning unit `u`.
- entities: abstract `abstractEntity1`, `abstractEntity2`; `entity1` extending both;
  `entity2` → `entity3` → `entity4` forming a three-deep single-inheritance chain.
- attributes on `entity1`: `a1` (string, required), `a2` (custom), `a3` (bool), `a4`
  (int, required + identifier), `a5` (measured).
- relations: `association` (1..1 → `entity4`), `containment` (0..* → `entity3`) and the
  pair `associationPartner1` (0..* → `entity4`, on `entity1`) / `associationPartner2`
  (0..1 → `entity1`, on `entity4`).

**Contracts a caller can violate**

- `associationPartner1` and `associationPartner2` are wired as each other's `partner` in
  both directions; changing one side alone leaves the PSM model invalid.
- `savePsmModel()` throws `PsmModel.PsmValidationException` when the assembled model
  fails PSM validation, so the fixture must stay metamodel-valid.
- Writes to the fixed path `FILE_LOCATION`; re-running overwrites the previous file.
