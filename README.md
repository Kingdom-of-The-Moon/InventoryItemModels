# Inventory Item Models

## Usage

- Create a new folder in your resource pack named "invmodels" (under [namespace]/assets/)
- Create a JSON file named after the item you want to replace models for (ex: "pumpkin.json")
- Add as many or as few transformation mode override entries as you want. Ex:
```
{
  "ground": "minecraft:item/pumpkin_seeds"
}
```
*This json replaces the model when rendering as an item on the round, with pumpkin seeds.*
- Optionally, you may specify to turn off lighting when the item is being rendered.
  - This is helpful when replacing a block model in the inventory with an item model.
```
{
  "gui": {
    "id": "minecraft:item/pumpkin_seeds",
    "disable_lighting": true
  }
}
```
*This json replaces the inventory item model with pumpkin seeds, and renders it without lighting, making it look like a regular item.*

## Transformation Mode Overrides
- If you are familiar with custom models in resource packs, these are the transformation options you can apply to your models.
- These overrides determine *when* an item's model should change, and *what* it should change to.
### Valid options include: 

- "none"
- "third_person_left_hand"
- "third_person_right_hand"
- "first_person_left_hand"
- "first_person_right_hand"
- "head"
- "gui"
- "ground"
- "fixed"

- Also, to mimic vanilla's behavior of tridents and spyglasses, they override "gui", "ground", and "fixed".
