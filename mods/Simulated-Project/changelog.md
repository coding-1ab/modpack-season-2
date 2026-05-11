# Additions
- Custom GUI for steering wheel when you're wearing goggles - *BlazerBo1*
- Add custom name support for typewriters
- Add comparator output for typewriters when they are in use
- Add config to hide "new ponder!" tooltip
- Add statistics to various items
- Add FE capability to docking connectors

# Changes
- Borehead Bearings no longer break blocks with no collision shape
- Refactor Docking Connector inventory implementation to work with new neoforge inventory changes
- Prevent fake players from using plunger launchers
- Adjust Optical Sensor formatting
- Remove unused blockstate in Altitude Sensor
- Velocity Sensor readout will no longer be negative
- Reduce Optical Sensor range by 0.5 blocks
- Make Laser Pointers use the center of the entity for their hit location instead of feet location.
- Prevent Swivel Bearings from updating their target angel if they are disassembled
- Balancing changes for hot air gusts
- Propellers no longer push entities that they don't have line of sight of
- Change formatting for ExtraKinetics information to account for localization

# Fixes
- Fix redstone inductor charge / discharge speed - *SeppeOngena*
- Model fix for Inductor - *Lucky56*
- Fix Borehead Bearings stalling when breaking certain blocks
- Fix Borehead Bearings occasionally crashing the client
- Fix Wheel Mounts not checking redstone on correct side
- Fix Linked Typewriter crash
- Fix a NPE crash in Springs
- Fix Spring shortening / lengthening issues
- Fix CC methods not properly using underlying value box numbers
- Fix crashes with some data components
- Fix airtight tags
- Fix hot air physics at high pressure gradients
- Fix client crash pertaining to Levitite catalyzing
- Fix some ExtraKinetics Issues