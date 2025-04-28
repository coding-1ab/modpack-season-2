## Tags
### Enchantment
* `create_enchantment_industry:blaze_enchanter/enchanting` contains all enchantments available in Blaze Enchanter regular enchanting, which includes `minecraft:in_enchanting_table`
* `create_enchantment_industry:blaze_enchanter/enchanting_exclusive` contains all enchantments exclusive to Blaze Enchanter regular enchanting
* `create_enchantment_industry:blaze_enchanter/super_enchanting` contains all enchantments available in Blaze Enchanter Super enchanting, which includes `create_enchantment_industry:blaze_enchanter/super_enchanting_exclusive` and `minecraft:in_enchanting_table` and excludes `create_enchantment_industry:blaze_enchanter/enchanting_exclusive`
* `create_enchantment_industry:blaze_enchanter/enchanting` contains all enchantments exclusive to Blaze Enchanter Super enchanting, which includes `minecraft:treasure` and excludes `minecraft:cures`. (Curse enchantments are still available when Blaze Forger is cursed)

## Recipe
###  Printing
`Printing` of Printer supports custom printing recipe. It has similar format as the recipe of Create, with type `create_enchantment_indusry:grinding`. Printing recipe requires extra `sound` field as Printing-Finish-Sound, for example `"sound": "item.book.page_turn"`.   
Template item is in 2nd position of input, printing material item is in 1st position of input.
### Grinding
`Grinding` of Mechanical Grindstone supports custom grinding recipe. It is the same format as the recipe of Create, with type `create_enchantment_indusry:printing`.   
Grinding can have fluid as input. If recipe has fluid as input, corresponding fluid in Grindstone Drain will be consumed.

## Data Maps
### Item
* `experience fuel`: Item can be fed to Blaze Experience Workstation as Experience. `"special": true` indicates Super Experience

### Fluid
* `printing/[type]`: Configure cost of builtin printing type
* `unit/experience`: Configure conversion ratio between experience fluid of other mods and Liquid Experience