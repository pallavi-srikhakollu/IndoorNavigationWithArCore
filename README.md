# IndoorNavigationWithArCore
Android app targeted to provide Indoor navigation using AR Core

This application uses ARCore provided by Google for Android. 

In this project we mainly use three API's provided by ARCore :

1.Image detection: For detecting if any image in front of camera is getting matched with stored images.

2.Object placement : For placing and detecting location of augumented images in given scene

3.Motion tracking : For tracking the motion of the user in a given scenrio, for tracking user info on Map.


Application flow:

1.When user installs the App, he is presented with a screen containg map, panned to his current location.
2.user can navigate to any location on the map, and click on add button to create the geofence around that area.
3.User can choose the radius and message for the particular geofence.
4.The created geofence is saved in the database.
5.When user enters that area, an local Notification is poped up.
6.on tap of notification user is navigated to scan the image using ARCore for authenticaion.
7.After scanning the image, user is presented with 4 blocks and he has to arrange them in order of Red--->Green and Blue --> Yellow.
8.Once placement is done he can lick on Authenticate button , if correct user is navigated to map screen, else given a chance to try again.
9.On the Map screen ,user can see a map, and camera feed . Once he starts walking he can see his current location with respect to map.


The application uses Geofencing API provided by Google to detect whether user has enetered a geofenced area.



