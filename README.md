# Sia Mobile
Sia Mobile serves as an Android client for [Sia](https://github.com/NebulousLabs/Sia), and provides a way for users to interact with the Sia network from their Android device. Sia has the potential to disrupt the cloud storage industry, and a successful mobile app for Sia will greatly help in making this a reality; Sia Mobile aims to be that.

Sia Mobile can
* set up and maintain a cold-storage wallet on your mobile device
* interact with a full node running on your computer
* run a full node on your mobile device
* coming soon: upload to and download from the Sia cloud storage network
* coming soon: SPV functionality (a.k.a. "lite wallet") once Sia implements this feature

[Sia Mobile on the Play Store](https://play.google.com/store/apps/details?id=vandyke.siamobile)

# Contributing
Contributions to the Sia Mobile codebase through this repository (pull requests, issue reporting, etc.) are welcomed, encouraged, and appreciated. Please view the [projects page](https://github.com/NickvanDyke/Sia-Mobile/projects) for a to-do list of changes and features for Sia Mobile. You do not have to adhere to this list, but it can be a helpful resource if you want to contribute but don't know in what way. My username on the [Sia Slack](siatalk.slack.com) is Nicktz. Please direct any questions or comments you have about contributing to me on there. The Sia Slack is also a great resource for any Sia-related questions that you may have while contributing to Sia Mobile. If you wish to contribute, please read the Licensing section of the Readme as well.

# Auditing
When Sia Mobile initially launched, the community showed some concern that they could not view the source code, given that it can interact with their Sia wallet if they set it up to do so. I understand this concern, and it's a huge part of why I decided to change Sia Mobile's GitHub repository from private to public. I have asked David Vorick, one of the core Sia developers, if they would be willing to look over Sia Mobile's code in order to ensure the community that it has nothing but good intentions. Understandably, he said that they don't have the time for that. So it's up to the community to audit the Sia Mobile codebase and spread the word that it exists and is trustworthy.

# Licensing
You may have noticed that this reposity does not have a license. This means that the work here is, by default, under exclusive copyright, and no one but the copyright holder may legally use, copy, distribute, or modify the contents of this repository without express permissiom. More info [here](https://choosealicense.com/no-license/). Contributions such as pull requests are still allowed, and greatly encouraged and appreciated. In such case, you will be asked to agree to a relatively short Contributor License Agreement (CLA). Agreeing to this CLA simply grants any rights you hold over your contribution to the project owner. This is purely so that I maintain ownership of this project that I have invested a lot of my time in (while also keeping it completely free and ad-free), and can ensure that I remain the sole distributor of Sia Mobile, through the Google Play Store or otherwise. I have absolutely no malicious intent with this lack-of-licensing and CLA. My goal in making Sia Mobile's GitHub repository public is for the community to have the ability to audit the code for trustowrthiness, and make contributions if they wish - which, again, is greatly encouraged and appreciated. To my understanding, this is the best way to do that while still maintaining ownership over the project and its distribution. If it is not, then please let me know so I can make changes to it.

As per section D5 of the [GitHub terms of service](https://help.github.com/articles/github-terms-of-service/), you may still view and fork this repository, as well as "use, display, and perform" its content. I believe this means you can legally clone the repository and build the Android APK from source if you wish. However, nowhere does it say you may make changes to your copy of its contents, nor redistribute them, and these rights are not expressively granted to you.
