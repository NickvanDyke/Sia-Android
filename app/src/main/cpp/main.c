//
// Created by Nick van Dyke on 1/4/18.
//
// Purpose of this file:
// Currently, the Sia node will only run on arm64 Android devices.
// This app is pretty useless without the Sia node (currently - maybe in
// the future Sia will have remote nodes or something). So obviously we only want
// arm64 devices to be able to download it from the play store.
// To do this, we include this native code, which allows us to build
// separate APKs for each ABI, and upload only the arm64-v8a one to the play store.
// This way, the app will only be visible and available for phones that can run
// arm64 code, which is what the siad binary is compiled as.
//
// TODO: I think it might be possible to compile siad (written using Goland) as a shared C library,
// and then write C code here that will make calls to that .so, and then write
// some JNI stuff so we can call the C code from Java/Kotlin. That will take a
// lot of work though. But maybe down the road. It might also enable running
// siad from within an iOS app, which isn't possible using an executable
// due to tighter restrictions.

int main() {
    return 0;
}