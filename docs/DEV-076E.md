# DEV-076E — General special-ability effect contracts/hooks

Status: IMPLEMENTED — Windows automated verification pending

## Goal
Give the provisional A/S ability IDs stable semantic integration points without freezing unresolved numeric balance values.

## Implemented hooks
- iron_formation -> infantry defense combat hook
- relentless_pursuit -> pursuit combat hook
- volley_fire -> archer firepower combat hook
- prosperous_domain -> strategic-point GOLD revenue hook
- production_management -> strategic-resource production hook
- supply_mastery -> army supply-efficiency hook
- fortress_command -> strategic-point defense hook
- supreme_command -> combined-arms combat hook
- grand_reform -> national administrative-capacity hook
- deep_campaign -> expedition/isolated-army hook

## Architecture
GeneralAbilityCatalog maps content IDs to GeneralAbilityHook. No percentages, flat bonuses, cooldowns or proc chances are embedded. Later balance configuration/policies can attach coefficients to these hooks and existing combat/economy/administrative services can consume them.

This avoids duplicating EconomyTick/StrategicPointProductionService/AdministrativeCapacityService/ArmySupplyService or the combat resolver.

## Still unresolved
- final numeric coefficients;
- whether some mixed abilities expose more than one hook;
- exact combat phase for combat hooks;
- point-capture behavior for assigned civil generals;
- authoritative general persistence/bootstrap ownership.
