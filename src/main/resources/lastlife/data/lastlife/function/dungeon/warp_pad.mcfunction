teleport @s @e[type=marker,limit=1,sort=random,x=0]
execute at @s if entity @e[limit=1,distance=..5,type=marker] run playsound minecraft:block.amethyst_block.chime master @s ~ ~ ~ 50 0.7
