# Sia Mobile
Sia Mobile serves as an Android client for [Sia](https://github.com/NebulousLabs/Sia), and provides a way for users to interact with the Sia network from their Android device. Sia has the potential to disrupt the cloud storage industry, and a successful mobile app for Sia will greatly help in making this a reality; Sia Mobile aims to be that.

Sia Mobile can:
* Send and receive Siacoin, the cryptocurrency used to buy cloud storage on the Sia network
* Generate a paper wallet
* Set up and maintain a local cold-storage wallet
* Interact with a full node running on your computer
* Run a full node on your device
* Coming soon: upload to and download from the Sia cloud storage network
* coming soon: SPV functionality (a.k.a. "lite wallet") once Sia implements this feature

[Sia Mobile on the Play Store](https://play.google.com/store/apps/details?id=vandyke.siamobile)

My username on the [Sia Slack](https://siatalk.slack.com) is Nicktz. Please direct any questions or comments you have to me on there, particularly in the #android-dev channel.

# Contributing
Sia Mobile is still in heavy development, and contributions to the Sia Mobile codebase through this repository (pull requests, issue reporting, etc.) are welcomed, encouraged, and appreciated. Please view the [projects page](https://github.com/NickvanDyke/Sia-Mobile/projects) for a to-do list of changes and features for Sia Mobile. This list is not exhaustive; not everything I want done is included. You also do not have to adhere to this list, but it can be a helpful resource if you want to contribute but don't know in what way. If you have any ideas for something you want to contribute that's not listed, I would be happy to hear them. Due to the lack of certain features of Sia right now, some things are currently impossible for Sia Mobile, such as a lite wallet. I've spoken with David Vorick extensively, and have a good understanding of what can and can't be done with Sia Mobile, so I recommend you run ideas by me first, to determine their viability. I would also be happy to help with implementation, at least as far as telling you what should be done, how, and where, and answering any questions you have.

If contributing, check out the Sia Slack's #android-dev channel. The Sia Slack is also a great resource for any Sia-related questions that you may have while contributing to Sia Mobile.

If you wish to contribute, please read the Licensing section of the Readme as well. Thank you!

# Auditing
When Sia Mobile initially launched, the community showed some concern that they could not view the source code, given that it can interact with their Sia wallet if they set it up to do so. I understand this concern, and it's a huge part of why I decided to change Sia Mobile's GitHub repository from private to public. I have asked David Vorick, one of the core Sia developers, if they would be willing to look over Sia Mobile's code in order to ensure the community that it has nothing but good intentions. Understandably, he said that they don't have the time for that. So it's up to the community to audit the Sia Mobile codebase and spread the word that it exists and is trustworthy.

# Licensing
You may have noticed that this reposity does not have a license. This means that the work here is, by default, under exclusive copyright of the repository owner, and no one but the copyright holder may legally use, copy, distribute, or modify the contents of this repository without express permission. More info [here](https://choosealicense.com/no-license/). However, as per section D5 of the [GitHub terms of service](https://help.github.com/articles/github-terms-of-service/), you may still view and fork this repository, as well as "use, display, and perform" its content. I believe this means you can legally clone the repository and build the Android APK from source if you wish (for yourself, not for redistribution). Note that the Github ToS does not include the right to make changes to forked content, nor to redistribute it. However, I grant you the right to make changes to the contents of your forked repository with the sole intent of contributing those changes to this repository through a pull request. You may not make changes for any other reason or purpose, and may not redistribute the source code nor the compiled APK. When making a pull request to this repository, you will be asked to agree to a relatively short Contributor License Agreement (CLA). Agreeing to [the CLA](https://gist.github.com/NickvanDyke/88e90b0cd9c95ff482b1ace258bd0e76) simply grants any rights you hold over your contribution to the project owner. This is purely so that I maintain ownership of this project that I have invested a lot of my time in (while also keeping it completely free and ad-free), and so that I can ensure that I remain the sole distributor of Sia Mobile, through the Google Play Store or otherwise. I have absolutely no malicious intent with this lack-of-licensing and CLA, and if others make significant contributions to Sia Mobile, I would happily include somewhere in the app a mention of them and their contributions. My goal in making Sia Mobile's GitHub repository public is for the community to have the ability to audit the code for trustworthiness, and make contributions if they wish - which, again, is greatly encouraged and appreciated. To my understanding, this is the best way to do that while still maintaining ownership over the project and its distribution. If it is not, then please let me know so I can make changes to it.

# Acknowledgements
Huge thanks to SiaPulse.com for a donation that made the time spent on Sia Mobile many times more possible.

[JGall1](https://github.com/JGall1) for implementing QR code generation and scanning.

[Gsora](https://github.com/gsora) for letting me use the AAR he created from [this](https://github.com/johnathanhowell/sia-coldstorage), which Sia Mobile uses to generate paper and cold storage wallets.

# Donations
Sia Mobile development is purely done for free/donations. Any amount is appreciated. Thanks!

Siacoin: 20c9ed0d1c70ab0d6f694b7795bae2190db6b31d97bc2fba8067a336ffef37aacbc0c826e5d3

BTC: 1G8Wzjk1d7ULXBpLTBHwK35kFb3VLGY4Fe

Ether: 0x0f35db6c049df0183716bd7696f467d4873583bb
