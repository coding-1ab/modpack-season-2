### Create: Enchantment Industry 2.1.1

#### Update
* Add dataMap for configuring cost multiplier of forging for each enchantment
* Add dataMap for configuring cost multiplier of splitting enchantment for each enchantment
* Add dataMap for configuring cost of printing Enchanted Book for each enchantment
* Add dataMap for configuring level extension for each enchantment. 
* Printer builtin functions can be disabled individually through config
* Printer printing enchanted book function has denylist
* Update Japanese Translation by @YukkuriOfuton

### Change
* Level extension of `Mending` is set to 0 due to the fact that all levels have same effect

#### Fix
* Fix Breaking a pipe-connected Blaze Forger or Blaze Enchanter causes crash
* Fix experience cake cutting recipe using wrong tag (@achookh)
* Fix Grindstone Drain, Blaze Forger and Blaze Enchanter do not have fluid handler when side is null
* Fix Grindstones missing Kinetic Stress Impact tooltip