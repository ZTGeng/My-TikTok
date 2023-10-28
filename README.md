# My TikTok

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Description

My TikTok is a personal project that demonstrates how ChatGPT can help in programming. It allows you to upload videos on your Android devices and explore the uploaded videos. Over 90% of the code is provided by ChatGPT, and around 50% of the debugging is finished by it.

The project contains an Android app and a Python Flask server. The server-side has no database and uses the operating system to organize videos so that you can easily manage them on your PC or Mac.

Please note that this project is not recommended for real-life usage and is intended only as a learning material.

## Installation

To install and run the project, follow these steps:

1. Clone the repository to your local machine.

2. Build and install the Android app on your Android device.

3. Install the required packages for the server by running the following command in the terminal:
```
pip install -r requirements.txt
```

4. Start the server by running the following command in the terminal:
```
python app.py
```

Note: By default, the server is configured to use `host='0.0.0.0', port=5000` to serve on all available network interfaces. This means that the server will be accessible from any device on the same network. However, this configuration has potential security risks and is intended for development purposes only. You can also change the port number to any other available number if needed.

5. Find the IP address of the server by running the following command in the terminal:

    - On macOS or Linux, run the following command:
    ```
    ifconfig
    ```

    - On Windows, run the following command:
    ```
    ipconfig
    ```

6. Input the server IP address in the app settings when you open the app for the first time.

## Usage

The Android app allows you to upload videos from your device, explore the uploaded videos, and play them. The Flask server provides the backend functionality for the app.

To get started, follow the installation instructions above. Make sure your Android device and the server are connected to the same local network.

Note: Only .mp4 videos are currently supported by the app.

## Contributing

While I am not currently accepting contributions to this project, I welcome any suggestions or feedback you may have. Please feel free to reach out to me with any comments or questions.

## License

This project is licensed under the terms of the MIT license. See the [LICENSE](LICENSE) file for details.

## Contact

For any questions or concerns, please contact me at zhtgeng@gmail.com.