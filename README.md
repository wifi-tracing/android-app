[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

<!-- PROJECT LOGO -->
<br />
<p align="center">
  <a href="https://github.com/wifi-tracing/android-app">
    <img src="docs/icon.png" alt="Logo" width="80" height="80">
  </a>

  <h3 align="center">Android App</h3>

  <p align="center">
    WiFi-based contact tracing app, developed as part of <a href="https://github.com/danilo-delbusso"><b>@danilo-delbusso</b></a>'s a final year project
    <br />
    <a href="https://github.com/wifi-tracing/android-app/issues">Report Bug</a>
  </p>
</p>


<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary><h2 style="display: inline-block">Table of Contents</h2></summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>


## About The Project

The app scans for WiFi signals around the user every 30 seconds, collecting relative location to WiFi hotspots locally.

The user can also decide to collect location data, which is used to localise hotspots and provide contagion spread data.

This data can be uploaded by scanning a specific QR code, aiding in contact tracing efforts.

Daily, the app downloads a copy of relevant scans from the [ingestion service](https://www.github.com/wifi-tracing/server), which will then be used to check for possible exposure to the virus.

<p align="center">
  <img alt="upload data page" src="docs/screenshot-1.png" width="30%">
&nbsp; &nbsp; 
  <img alt="developer settings page" src="docs/screenshot-2.png" width="30%">
  &nbsp; &nbsp;
    <img alt="main page" src="docs/screenshot-3.gif" width="30%">

</p>

<!-- GETTING STARTED -->
## Getting Started

To get a local copy up and running follow these simple steps.

### Prerequisites

In order to run a local copy, you'll need to install the following:

* [Android Studio](https://developer.android.com/studio/install)

### Installation

Installation can be done by either building the project, or downloading the APK.

<!-- ROADMAP -->
## Roadmap

See the [open issues](https://github.com/wifi-tracing/android-app/issues) for a list of proposed features (and known issues).


<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create.


The repo itself is just used to show the project. It is **NOT** actively maintained. The author suggests forking the project instead of opening new issues.

<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE` for more information.

<!-- CONTACT -->
## Contact

Danilo Del Busso - [@danilo_delbusso](https://twitter.com/danilo_delbusso)


### Attributions

Coronavirus icons:
<div>Icons made by <a href="https://www.freepik.com" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a></div>

Material Icons:
<div>Icons made by <a href="https://www.flaticon.com/authors/google" title="Google">Google</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a></div>

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/wifi-tracing/android-app.svg?style=for-the-badge
[contributors-url]: https://github.com/wifi-tracing/android-app/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/wifi-tracing/android-app.svg?style=for-the-badge
[forks-url]: https://github.com/wifi-tracing/android-app/network/members
[stars-shield]: https://img.shields.io/github/stars/wifi-tracing/android-app.svg?style=for-the-badge
[stars-url]: https://github.com/wifi-tracing/android-app/stargazers
[issues-shield]: https://img.shields.io/github/issues/wifi-tracing/android-app.svg?style=for-the-badge
[issues-url]: https://github.com/wifi-tracing/android-app/issues
[license-shield]: https://img.shields.io/github/license/wifi-tracing/android-app.svg?style=for-the-badge
[license-url]: https://github.com/wifi-tracing/android-app/blob/master/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/danilo-delbusso/
