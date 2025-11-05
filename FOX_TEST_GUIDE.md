# Fox Delivery Oscillation Test Script

This document describes how to reproduce and test the fox delivery oscillation bug using the `/foxtest` command.

## Overview
The bug occurs when a delivery fox overshoots its target destination, causing it to oscillate between "DELIVERING" and "WAITING" states in a loop.

## Prerequisites
- Server running with the LastLife plugin
- Admin permissions (`lastlife.admin.foxtest`)
- Access to in-game commands

## Test Commands

### Basic Test (Most likely to reproduce the bug)
```
/foxtest spawn
```
This spawns a fox 10 blocks behind you that will deliver to your current location. The fox will likely overshoot due to momentum, potentially triggering the oscillation.

### Variable Distance Test
```
/foxtest spawn 15
```
Spawn a fox at a specific distance (1-50 blocks). Higher distances increase the chance of overshooting.

### Precise Coordinate Test
```
/foxtest precise 100 64 200
```
Spawn a fox to deliver to exact coordinates. Useful for testing specific problematic locations.

### Cleanup
```
/foxtest cleanup
```
Removes all test foxes within 100 blocks to clean up after testing.

## How to Reproduce the Oscillation Bug

1. **Setup**: Stand in an open area with room for the fox to approach
2. **Spawn**: Use `/foxtest spawn 15` to create a fox with good momentum
3. **Observe**: Watch the fox approach your location
4. **Look for Bug**: 
   - Fox should reach your location and sit (WAITING state)
   - If bugged: fox will stand up and start moving again (DELIVERING state)
   - Then sit again when it reaches you (WAITING state)
   - This creates an endless loop of standing/sitting

## Expected Behavior (After Fix)

With the velocity zeroing fix applied:
1. Fox approaches target location
2. Fox enters WAITING state and sits
3. Fox stays sitting (no oscillation)
4. Fox remains in WAITING state until interacted with

## Testing the Fix

### Before the fix:
- Run `/foxtest spawn 20`
- Watch for oscillation (fox repeatedly standing/sitting)
- Fox nameplate might flicker between states

### After the fix:
- Run the same command
- Fox should sit and stay sitting
- No oscillation should occur

## Advanced Testing

### Edge Cases to Test:
1. **Steep terrain**: Test on hills where fox might overshoot due to gravity
2. **Obstacles**: Place blocks near target to see if fox gets confused
3. **Multiple foxes**: Spawn several foxes to the same target
4. **Moving targets**: Use precise coordinates then move away

### Monitoring State Changes:
- Watch the fox's sitting animation (visual indicator of state)
- Check for any "jittery" movement at the destination
- Verify fox stays put when reaching target

## Performance Testing
```bash
# Spawn multiple foxes for stress testing
/foxtest spawn 5
/foxtest spawn 10  
/foxtest spawn 15
/foxtest spawn 20
# Then cleanup
/foxtest cleanup
```

## Troubleshooting

### If command doesn't work:
- Check you have `lastlife.admin.foxtest` permission
- Verify the plugin loaded successfully
- Check server logs for any errors

### If fox doesn't spawn:
- Make sure you're in a valid world
- Check there's enough space above you
- Verify spawn location isn't in solid blocks

### If oscillation still occurs:
- The fix may need additional adjustments
- Try increasing the arrival tolerance in `reachedTarget()`
- Consider adding a minimum sitting time before state changes

## Code Location
The fix is in: `src/main/java/com/dragon/lastlife/nms/CustomFox.java`
- Method: `tick()` in the `DELIVERING` case
- Added: `this.setDeltaMovement(new Vec3(0, 0, 0));` after navigation stops