# Msitu
Msitu is a tree planting app meant to simplify the pegging and rope-tying process, during tree planting.
The app is used in conjunction with a RTK Rover and RTK base station. This allows us to map statelite provided coordinates onto the ground with an error margin margin of just 1cm.

## License
This project has a GPL License, and it is attached in the repository.

## Installation.
The project is ready to use out of the box, once you clone and run it in Android studio. 

** How to clone:
*mkdir `myDir`
*cd `myDir`
*`git clone https://github.com/kitandara/kibira.git`
Open the project using android studio, and wait for gradle to download the dependencies and build the project.
Android studio `Dolphin` was used for the project, but the plugins were successfully compatible with `Chipmuck` ,`Bumblebee` and upgraded to be compatible with 'Dolphin`.

If it does not successfully build, the below could be the cause. 
* Note that the minimum sdk is `23` and target sdk is `32`.
* The project uses the MVVM Model so ensure that `View Binding` is enabled in your gradle scripts.
* In case of serialization issues, add "org.jetbrains.kotlin:kotlin-serialization:1.6.10" as a classpath in your project `gradle` file, under `buildScript` section.
* In addition, don't forget to add 'org.jetbrains.kotlin.plugin.serialization' and 'kotlin-kapt' plugins in your app `gradle` file, under the `plugin` section.

## Basic Usage.
* Start with setting up the equipment. PS. Ensure that the base station has a clear view of the sky- For accurate statelite readings.
* Once both the base station and the rover are communicating, turn on the app.
* Connect the app to the rover either using Bluetooth or USB and wait until it indicates that you have an RTK Fix. This takes a few minutes.

## How to start project.
* You will need to capture two points which will act as base points, from which all other planting lines will be based.
* In order to do this, make sure that you have an rtk fix established. The first thing to do is to place two pegs on the outskirt of the plot to be 
planted;keep them at a distance of about 50m and endeavour to mimick the orientation in which you desire to plant the trees. Tie a rope on each end of the pegs to ensure straightness.
* With the rope in place, using the app, copy any two coordinate values that lie on the rope. Keep them as far apart from eachother as you can. 
* Noow create a new project by filling in the necessary fields, and pasting those points into the `Basepoints` field. This will create a project and draw planting lines as well.

* PS. If you have no internet connection, It is advised that you move around on the plot with the rover, just to establish a bearing. (Since you don't have a visual map to give you a visual representation.


## Syncing different equipment sets.
If using two different sets of equipment, we advise the below to be followed.
* Create a project `Project A` using  equipment `Set One`.
* Using the app, draw out all the possible lines that can be handled by `Project A`.
* Stand on the last line of `Project A` ;using the second device which is connected to the second set of equipment, copy any two coordinates off of that line.( These will act as the new base points for `Project B` which will be loaded with `Set two`.)
* After copying the new base points from `Project A`, simply create a project, all that is all to it.
* `Project B` will be the same project as `Project A` from equipment `Set one`;Almost like an extension of it, despite the fact that two different equipment sets were used. The planting lines will be right after the last line drawn by equipment `Set one`, with a negligible offset of about 1-2 ft.


## Resources
* [What is rtk?]( https://www.youtube.com/watch?v=257WX_agvtg)
* [How The rover and base work?](https://www.youtube.com/watch?v=Rk09oMD_I24&t=4s)
