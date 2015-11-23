# discomatic
This is a practice program used to find circles in static images or images fed from a webcam using the Sobel operator and the Hough Transform.
The process of finding them can be summarised as it follows:

* Scale the image down if it is too big
* Apply a gaussian blur using a [convolution matrix](https://en.wikipedia.org/wiki/Kernel_(image_processing)) to reduce noise
* Apply the [Sobel operator](https://en.wikipedia.org/wiki/Sobel_operator) to find edges
* Discard edge pixels based on a predefined threshold
* Apply the [Hough transform](https://en.wikipedia.org/wiki/Hough_transform#Circle_detection_process) to find the most likely candidate for a circle using the [midpoint circle algorithm](https://en.wikipedia.org/wiki/Midpoint_circle_algorithm) to find the pixels that should be considered
* Draw the circle on the original image

Capturing live images is done by using [webcam-capture](https://github.com/sarxos/webcam-capture) and Swing to display the images to a GUI.

Loading images from files is done by passing the folders or files as arguments when launching the binary and it will launch in text mode and process.
