# kibira
Kibira is a tree planting app meant to simplify the pegging and rope-tying process, during tree planting.
The app is used in conjunction with a RTK Rover and RTK base station. This allows us to map statelite provided coordinates onto the ground with an error margin margin of just 1cm.

## License
This project has a GPL License, and it is attached in the repository.

## Installation.
The project is ready to use out of the box, once you clone and run it in Android studio. 
Android studio `Dolphin` was used for the project, but the plugins were successfully compatible with `Chipmuck` and `Bumblebee`

** How to clone:
In your desired directory, clone the repository using `git clone https://github.com/kitandara/kibira.git -b main`
Open the project using android studio, and wait for gradle to download the dependencies and build the project.
If it does not successfully build, the below could be the cause. 
* Note that the minimum sdk is `23` and target sdk is `32`.
* The project uses the MVVM Model so ensure that `View Binding` is enabled in your gradle scripts.
* In case of serialization issues, add "org.jetbrains.kotlin:kotlin-serialization:1.6.10" as a classpath in your project `gradle` file, under `buildScript` section.
* In addition, don't forget to add 'org.jetbrains.kotlin.plugin.serialization' and 'kotlin-kapt' plugins in your app `gradle` file, under the `plugin` section.

## Basic Usage.
* Start with setting up the equipment. PS. Ensure that the base station has a clear view of the sky- For accurate statelite readings.
* Once both the base station and the rover are communicating, turn on the app.
* Connect the app to the rover either using Bluetooth or USB and wait untill it indicates that you have an RTK Fix. This takes between a few minutes.

## How to start project.
* Create a new project by filling in the necessary fields.
However to draw lines, there are two options.
If you have an internet connection, then you can have a visual map of where your location. With this, a simple tap on the screen, in the direction you want the lines to drawn will do.
But if you have no internet connection, then there's a work around.
* Place two pegs anywhere on the outskirt of the plot you are going to plant, using a rope, mimic a straight line using these two pegs as a reference.
* Copy these two coordinates and export that project.
* Using a `json editor` such as [json kitty](https://play.google.com/store/apps/details?id=codefeverr.json_kitty&hl=en&gl=US&pli=1), open that project.
* Replace the base points with the new coordinates that you have copied and save the project.
* Open the app again, tap `Import Project`. Here, the updated version of the project will be available. Once you open it, it will have the corresponding planting lines, with reference to the points/pegs you used in the beginning.
* PS. It is advised that yo move around on the plot with the rover, just to establish a bearing. (Since you don't have a visual map to give you a visual representation.


## Syncing different equipment sets.
If using two different sets of equipment, we advise the below to be followed.
* Create a project `Project A` using  equipment `Set One`.
* Using the app, draw all the possible lines that can be handled by `Project A`.
* Stand on the last line of `Project A` and copy any two coordinates off of that line.( These will act as the new base points for `Project A` which will be loaded with `Set two`.)
* After copying the new base points, export `Project A`. It will be saved wherever your downloads are located on your phone/tablet.
* Transfer `Project A` onto the phone/tablet you are going to use with equipment `Set Two`.
* Open it using a `json editor` such as [json kitty](https://play.google.com/store/apps/details?id=codefeverr.json_kitty&hl=en&gl=US&pli=1)
* Replace the existing base points of `Project A` with the copied base points and save it.
* Open the app, when you tap `Import project` the version of `Project A` with the new base points, will be available. This will successfully be the same project as `Project A` from equipment `Set one` despite the fact that two different equipment sets were used. The planting lines will be right after the last line drawn by equipment `Set one`, with a negligible offset of about 1-2 inches.


## Resources
* [What is rtk?]( https://www.youtube.com/watch?v=257WX_agvtg)
* [How The rover and base work?](https://www.youtube.com/watch?v=Rk09oMD_I24&t=4s)
