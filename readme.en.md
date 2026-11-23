# Applied exercise: "Parallel temperature bars"

Two threads read temperatures, one for Las Palmas and one for Helsinki. Each thread computes its own city's average and increments its own counter. The main program is the only one that draws: it reads the counters and prints two ASCII progress bars. The length of a bar is the share of that city's readings processed so far. The average is printed next to the bar as a number.

## Starting the exercise

First fork this repository into your own GitHub repository with GitHub's **Fork** button, and only then clone your fork to your computer:

```sh
git clone https://github.com/<your-github-username>/10-parallel-temps.git
```

Open the cloned project in your code editor.

## Testing

The parts of the exercise have ready-made [JUnit](https://junit.org/) unit tests in the [src/test/java](./src/test/java/) directory. Each part tells which test class grades it. You can run the tests with your code editor's testing tool ([VS Code](https://code.visualstudio.com/docs/java/java-testing), [Eclipse](https://www.vogella.com/tutorials/JUnitEclipse/article.html)) or with [Gradle](https://docs.gradle.org/current/userguide/java_testing.html), for example:

```
./gradlew test --tests ShowProgressTest       # unix
.\gradlew.bat test --tests ShowProgressTest   # windows
```

Add `--info` to the end of the command for more details about failing tests. You do not need to study how the tests are implemented. You may write more tests of your own, but do not change or remove the given tests.

Do not change the names, parameters or return types of the given classes, methods and constructors, because the tests will then not compile. You get the original version of a file back with `git restore <path to file>` or with the **Palauta lähtökoodi** (restore starter code) function of the course system.

## Submitting and points

Each part of the exercise has its own page in the course system (https://jtjuslin.kapsi.fi/java), and its code fields contain the part's starter code. Copy your solution into the code fields or write it there directly. **Testaa ilman palautusta** (test without submitting) runs the part's tests without saving points, and **Lähetä** (submit) submits your solution. Each test class is accepted only if all of its tests pass, and the points are divided according to the accepted test classes.

**Points in two stages**

1. When your solution passes in the course system, you get **half of the points** for the part.
2. Finally, push everything to your public GitHub repository. Then press the **Lähetä tarkistettavaksi** (submit for checking) button on the part's page. When the tests pass, you get **full points** for the part. The repository may remain public.

There is no grading YAML workflow (GitHub Actions) on GitHub, and `git push` does not start any grading. When you press **Lähetä tarkistettavaksi**, the course system's bot fetches your code from your repository and checks it with the same tests. Both stages must be completed before the chapter deadline. You can submit as many times as you need, and your best result is kept.


## Temperatures

[data/las-palmas.csv](./data/las-palmas.csv) contains the daily mean temperatures for Las Palmas and [data/helsinki.csv](./data/helsinki.csv) those for Helsinki, both for 2024. Do not modify the files. One row is one reading, in the form `date,temperature`. Comment lines start with `#`.

The figures are daily means from the Open-Meteo Historical Weather API ([open-meteo.com](https://open-meteo.com/)), based on the ERA5 reanalysis (Copernicus / ECMWF). The requested coordinates are 28.1235 N, 15.4363 W for Las Palmas and 60.1699 N, 24.9384 E for Helsinki. The full-year average is about 21 °C in Las Palmas and about 7 °C in Helsinki. The tests accept a Las Palmas average from 19 to 24 °C and a Helsinki average from 3 to 9 °C. Helsinki is compared with a wider tolerance than Las Palmas.

The readings are loaded by the ready-made class [TemperatureData](./src/main/java/temps/TemperatureData.java). The [TemperatureSource](./src/main/java/temps/TemperatureSource.java) interface is the place where a separate feeder program can be plugged in later. [QueueTemperatureSource](./src/main/java/temps/QueueTemperatureSource.java) accepts one reading at a time from another thread: the feeder calls `publish` and finally `close`, and the worker only calls `next`. In this exercise the bundled file is enough. A delay between readings belongs to the feeder, not to the worker. The tests use a delay of `0`, so the readings are available immediately.

## How the bars are drawn

Each worker has its own `volatile int checked`. The worker increments it by one whenever it has processed one temperature. This is the only state the main thread reads while the worker is still running. The worker prints nothing and does not call `Thread.sleep`.

The main thread starts both threads, and in a loop it:

1. reads both `checked` values,
2. draws both bars with `showProgress`,
3. calls `Thread.sleep` before the next frame.

The length of a bar is the share of readings processed, not the temperature. The average may be shown next to the bar as a number. When both workers are done, the main thread calls `join()` on each of them and only then reads the final averages and draws the last frame with `showProgress`. The last frame is the result the tests check.

Examples of frames. Zero percent:

```
Las Palmas | --------------------   0 %   0.0 °C
Helsinki   | --------------------   0 %   0.0 °C
```

When 147 of 366 Las Palmas readings have been processed (40 %) and 55 Helsinki readings (15 %), the running averages of the 2024 data look like this:

```
Las Palmas | ########------------  40 %  19.5 °C
Helsinki   | ###-----------------  15 %  -5.9 °C
```

When both are finished (100 %), the full-year averages are:

```
Las Palmas | #################### 100 %  20.7 °C
Helsinki   | #################### 100 %   7.1 °C
```

`#` is the processed share and `-` is the share still left. A bar is always 20 characters. The decimal separator is a dot. The unit is degrees Celsius.

You can run the program in your code editor or with `./gradlew run` (on Windows `.\gradlew.bat run`). The `main` method feeds the readings with a short delay so the bars have time to update. The tests call `runChart(0, 0)` with no delay.


## Part 1: showProgress *(applying, 30 %)*

Implement the `showProgress` method of the [Main](./src/main/java/temps/Main.java) class. It receives the names of two cities, the counts of processed readings, the totals and the averages, and returns a two-line string. The method does not print by itself. The lines are separated by `\n`. The string has no trailing newline and no terminal control codes.

One line is built like this:

* the city name is padded to a width of 10 (`%-10s`),
* then ` | `,
* the bar is `Main.BAR_WIDTH` characters (20). The filled length is `checked * BAR_WIDTH / total` characters of `#` (integer division) and the rest are `-`,
* the percentage is `checked * 100 / total`, printed in a width of three characters,
* the average is printed with one decimal, in a width of five, using `Locale.ROOT`, followed by ` °C`.

If `total` is 0, the percentage and the bar are zero. A more detailed description is in the method's comments.

Graded with the test class [ShowProgressTest](./src/test/java/temps/ShowProgressTest.java). The tests call `showProgress` directly at 0 %, at partial progress and at 100 %.


## Part 2: TemperatureWorker *(advanced, 40 %)*

Implement [TemperatureWorker](./src/main/java/temps/TemperatureWorker.java). The class extends `Thread`. The field `checked` is given: it is a `volatile int`, and it is incremented by one for every processed reading.

`run` reads `TemperatureSource.next()` until the source returns an empty `OptionalDouble`. Each reading is added to the sum before `checked` grows. The method must not print or sleep.

`average` returns the arithmetic mean of the processed readings. If no reading has been processed, the result is `0`. The average is reliable only after `join()`.

Graded with the test class [TemperatureWorkerTest](./src/test/java/temps/TemperatureWorkerTest.java). The tests check that the class is a thread, that `checked` is `volatile`, that the counter advances one reading at a time, that the average changes as new readings arrive, that two threads run at the same time, and that the averages of the bundled data fall inside the allowed ranges.


## Part 3: the main loop *(advanced, 30 %)*

Implement `Main.runChart`. It opens both CSV files with `TemperatureData.open`, starts a thread for `Las Palmas` and a thread for `Helsinki`, draws frames with `showProgress` for as long as either thread is alive, and sleeps between frames with `Thread.sleep(redrawDelayMillis)`. It then calls `join()` on both and only then reads `average()` and returns the final frame.

Do not move drawing or sleeping into the workers. The parameter `feedDelayMillis` is the delay between readings; the tests pass `0`.

Graded with the test class [ParallelTempsTest](./src/test/java/temps/ParallelTempsTest.java). It runs `runChart(0, 0)` and checks the final frame: both bars are at 100 %, the cities are labelled, and the averages fall inside the ranges above. Helsinki's tolerance is wider than Las Palmas's.


## License and authors

This exercise was written by Jukka Juslin on top of the structure of the course's earlier exercise repositories, and it is licensed under the [Creative Commons BY-NC-SA license](https://creativecommons.org/licenses/by-nc-sa/4.0/). The temperatures were retrieved from the Open-Meteo Historical Weather API and are based on the ERA5 reanalysis (Copernicus Climate Change Service / ECMWF). Open-Meteo license: [open-meteo.com/en/license](https://open-meteo.com/en/license).
