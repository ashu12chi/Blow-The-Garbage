# Blow the Garbage
[![Open Source Love](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/ellerbrock/open-source-badges/)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)<br>
This is the official Droidrush repository of NPDevs team (Avishkar-2019--Annual Techfest of MNNIT Allahabad) <br>
**This app won fourth prize and best among second year in Droidrush 2019**<br>
**Webpage:** [https://nisiddharth.github.io/Blow-The-Garbage/](https://nisiddharth.github.io/Blow-The-Garbage/)

**Project Presentation:** [View here](https://github.com/nisiddharth/Blow-The-Garbage/blob/master/BlowTheGarbage.pdf)

**App's APK:** [Download here](https://github.com/nisiddharth/Blow-The-Garbage/blob/master/BlowTheGarbage.apk)


## Welcome to Swachh Bharat
- This app is developed to help the cleaners clean the messy roads
- It provides an easy platform to people to accomplish the Swachha Bharat Mission
- It also attempts of reducing the load over officials by Machine Learning

## The Process Flow
**For User:**
1. Login/Sign Up
2. Select garbage proximity
3. Accordingly select/search location (on Map), capture/upload image or upvote existing garbage point
4. ML prediction happens
5. In case of false prediction send Image for verification to the Admin
6. Feedback/complaint/review submission.

**For Admin:**
1. Authority to add/delete cleaner
2. Verify garbage cleaning requests (notified by the app vie notification channel)
3. Read reviews/feedbacks/complaints
4. View cleaners’ performance data
5. View graphs/stats related to monthly/yearly cleaning requests.

**For Cleaner:**
1. App notifies about newly added gapbage collection point in their alloted area and they can view them
2. App gives them shortest path to traverse so that all garbage is collected in one ride.
3. View one’s own stats/graphs.

## APIs and Components used
- Google Maps API
- Mapbox Maps API
- Firebase Realtime Database
- Firebase Authentication
- Firebase Storage
- Tensorflow
- Tensorflow Lite
- MPAndroidChart by PhilJay for interactive graphs
- AndroidX artifacts with Google Material Design components

## Blow the Garbage
**Team:** NPDevs

**Members:**
1. [Ashutosh Chitranshi](https://github.com/ashu12chi/)
2. [Nishchal Siddharth Pandey](https://github.com/nisiddharth/)

## To try hands on the project
Either, just [download the APK](https://github.com/nisiddharth/Blow-The-Garbage/blob/master/BlowTheGarbage.apk) and try it out.
<br>
Or,
<br>
1. Clone the project using link: https://github.com/nisiddharth/Blow-The-Garbage.git
2. Import the project in Android Studio
3. Generate your own Google Maps API key and Mapbox API key and add it to project in strings.xml (these locations are properly commented in the project)
4. Deploy the app to your Android device and viola... you are good to go!
