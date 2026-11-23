> [!NOTE]
> The English translation for this exercise can be found in [this file](./readme.en.md).

# Soveltava tehtävä: "Rinnakkaiset lämpötilapalkit"

Kaksi säiettä lukee lämpötiloja, toinen Las Palmasista ja toinen Helsingistä. Kumpikin säie laskee oman kaupunkinsa keskiarvon ja kasvattaa omaa laskuriaan. Pääohjelma on ainoa, joka piirtää: se lukee laskurit ja tulostaa kaksi ASCII-edistymispalkkia. Palkin pituus kertoo, kuinka suuri osuus kyseisen kaupungin havainnoista on käsitelty. Keskiarvo tulostetaan palkin viereen lukuna.

## Tehtävän aloittaminen

Forkkaa ensin tämä repositorio omaan GitHub-repositorioosi GitHubin **Fork**-painikkeella ja kloonaa vasta sitten oma forkkisi koneellesi:

```sh
git clone https://github.com/<oma-github-tunnuksesi>/10-parallel-temps.git
```

Avaa kloonattu projekti koodieditorissasi.

## Testaaminen

Tehtävän osille on valmiit [JUnit](https://junit.org/)-yksikkötestit hakemistossa [src/test/java](./src/test/java/). Kunkin osan kohdalla kerrotaan, millä testiluokalla se arvioidaan. Voit ajaa testit koodieditorisi testaustyökalulla ([VS Code](https://code.visualstudio.com/docs/java/java-testing), [Eclipse](https://www.vogella.com/tutorials/JUnitEclipse/article.html)) tai [Gradlella](https://docs.gradle.org/current/userguide/java_testing.html), esimerkiksi:

```
./gradlew test --tests ShowProgressTest       # unix
.\gradlew.bat test --tests ShowProgressTest   # windows
```

Lisää komennon loppuun `--info`, jos haluat tarkemmat tiedot epäonnistuneista testeistä. Testien tekniseen toteutukseen ei tarvitse perehtyä. Saat kirjoittaa omia testejä lisää, mutta älä muuta tai poista valmiita testejä.

Älä muuta valmiiden luokkien, metodien ja konstruktorien nimiä, parametreja tai paluuarvojen tyyppejä, sillä silloin testit eivät käänny. Saat tiedoston alkuperäisen version takaisin komennolla `git restore <tiedoston polku>` tai kurssijärjestelmän **Palauta lähtökoodi** -toiminnolla.

## Palauttaminen ja pisteet

Jokaisella tehtävän osalla on kurssijärjestelmässä (https://jtjuslin.kapsi.fi/java) oma sivunsa, jonka koodikentissä on osan koodipohja. Kopioi ratkaisusi koodikenttiin tai kirjoita se suoraan niihin. **Testaa ilman palautusta** ajaa osan testit tallentamatta pisteitä, ja **Lähetä** palauttaa ratkaisun. Jokainen testiluokka hyväksytään vain, jos sen kaikki testit menevät läpi, ja pisteet jaetaan hyväksyttyjen testiluokkien mukaan.

**Pisteet kahdessa vaiheessa**

1. Kun ratkaisusi menee läpi kurssijärjestelmässä, saat osasta **puolet pisteistä**.
2. Pushaa lopuksi kaikki julkiseen GitHub-repositorioosi. Paina tämän jälkeen osan sivulla **Lähetä tarkistettavaksi** -nappia. Kun testit menevät läpi, saat osasta **täydet pisteet**. Repositorio saa jäädä julkiseksi.

GitHubissa ei ole arvioivaa YAML-tehtävävirtaa (GitHub Actions -workflowta), eikä `git push` käynnistä arviointia. Kun painat **Lähetä tarkistettavaksi**, kurssijärjestelmän botti hakee koodisi repositoriostasi ja tarkistaa sen samoilla testeillä. Molemmat vaiheet pitää tehdä ennen luvun määräaikaa. Voit lähettää ratkaisusi niin monta kertaa kuin tarvitset, ja voimaan jää paras tulos.


## Lämpötilat

Tiedosto [data/las-palmas.csv](./data/las-palmas.csv) sisältää Las Palmasin ja [data/helsinki.csv](./data/helsinki.csv) Helsingin päiväkeskilämpötilat vuodelta 2024. Älä muokkaa tiedostoja. Yksi rivi on yksi havainto, muodossa `päivä,lämpötila`. Kommenttirivit alkavat merkillä `#`.

Luvut ovat Open-Meteo Historical Weather API:n ([open-meteo.com](https://open-meteo.com/)) ERA5-uusanalyysiin perustuvia päiväkeskiarvoja (Copernicus / ECMWF). Las Palmasin pyydetyt koordinaatit ovat 28.1235 N, 15.4363 W ja Helsingin 60.1699 N, 24.9384 E. Koko vuoden keskiarvo on Las Palmasissa noin 21 °C ja Helsingissä noin 7 °C. Testit hyväksyvät Las Palmasin keskiarvon väliltä 19–24 °C ja Helsingin keskiarvon väliltä 3–9 °C. Helsingin vertailussa on väljempi toleranssi kuin Las Palmasin.

Havainnot luetaan valmiilla luokalla [TemperatureData](./src/main/java/temps/TemperatureData.java). Rajapinta [TemperatureSource](./src/main/java/temps/TemperatureSource.java) on se kohta, johon myöhemmin voi kytkeä erillisen syöttöohjelman. [QueueTemperatureSource](./src/main/java/temps/QueueTemperatureSource.java) ottaa vastaan yksittäisiä lukemia toisesta säikeestä: syöttäjä kutsuu `publish`-metodia ja lopuksi `close`-metodia, työntekijä vain `next`-metodia. Tässä tehtävässä riittää mukana tuleva tiedosto. Viive havaintojen välissä kuuluu syöttäjälle, ei työntekijälle. Testit käyttävät viivettä `0`, jolloin havainnot ovat heti saatavilla.

## Miten palkit piirretään

Kummallakin työntekijällä on oma `volatile int checked`. Työntekijä kasvattaa sitä yhdellä aina, kun se on käsitellyt yhden lämpötilan. Tämä on ainoa tila, jota pääsäie lukee työntekijän vielä käydessä. Työntekijä ei tulosta mitään eikä kutsu `Thread.sleep`-metodia.

Pääsäie käynnistää molemmat säikeet, ja silmukassa:

1. lukee molempien `checked`-arvot,
2. piirtää molemmat palkit metodilla `showProgress`,
3. kutsuu `Thread.sleep`-metodia ennen seuraavaa ruutua.

Palkin pituus on käsiteltyjen havaintojen osuus, ei lämpötila. Keskiarvo saa näkyä palkin vieressä lukuna. Kun molemmat työntekijät ovat valmiit, pääsäie kutsuu kummallekin `join()` ja vasta sen jälkeen lukee lopulliset keskiarvot ja piirtää viimeisen ruudun `showProgress`-metodilla. Viimeinen ruutu on se tulos, jonka testit tarkistavat.

Esimerkkejä ruuduista. Nolla prosenttia:

```
Las Palmas | --------------------   0 %   0.0 °C
Helsinki   | --------------------   0 %   0.0 °C
```

Kun Las Palmasista on käsitelty 147 havaintoa 366:sta (40 %) ja Helsingistä 55 (15 %), vuoden 2024 datan juoksevat keskiarvot näyttävät tältä:

```
Las Palmas | ########------------  40 %  19.5 °C
Helsinki   | ###-----------------  15 %  -5.9 °C
```

Kun molemmat ovat valmiit (100 %), koko vuoden keskiarvot ovat:

```
Las Palmas | #################### 100 %  20.7 °C
Helsinki   | #################### 100 %   7.1 °C
```

`#` on käsitelty osuus ja `-` on jäljellä oleva osuus. Palkissa on aina 20 merkkiä. Desimaalierotin on piste ja yksikkö on celsiusaste.

Voit suorittaa ohjelman koodieditorissasi tai komennolla `./gradlew run` (Windowsissa `.\gradlew.bat run`). `main`-metodi syöttää havainnot pienellä viiveellä, jotta palkit ehtivät päivittyä. Testit kutsuvat `runChart(0, 0)` ilman viivettä.


## Osa 1: showProgress *(soveltava, 30 %)*

Toteuta [Main](./src/main/java/temps/Main.java)-luokan metodi `showProgress`. Se saa kahden kaupungin nimet, käsiteltyjen havaintojen määrät, havaintojen kokonaismäärät ja keskiarvot, ja palauttaa kahden rivin merkkijonon. Metodi ei tulosta itse. Rivien välissä on `\n`, eikä merkkijonon lopussa ole rivinvaihtoa eikä päätteen ohjauskoodeja.

Yksi rivi kootaan näin:

* kaupungin nimi täytetään 10 merkin levyiseksi (`%-10s`),
* sen jälkeen tulee ` | `,
* palkki on `Main.BAR_WIDTH` merkkiä (20). Täyttöä on `checked * BAR_WIDTH / total` kappaletta merkkiä `#` (kokonaisjako) ja loput ovat `-`,
* prosentti on `checked * 100 / total`, tulostettuna kolmen merkin levyisenä,
* keskiarvo tulostetaan yhdellä desimaalilla, viiden merkin levyisenä, luokalla `Locale.ROOT`, ja perään tulee ` °C`.

Jos `total` on 0, prosentti ja palkki ovat nollia. Tarkempi kuvaus on metodin kommenteissa.

Arvioinnissa ajetaan testiluokka [ShowProgressTest](./src/test/java/temps/ShowProgressTest.java). Testit kutsuvat `showProgress`-metodia suoraan arvoilla 0 %, osittaisella etenemisellä ja 100 %:lla.


## Osa 2: TemperatureWorker *(edistynyt, 40 %)*

Toteuta [TemperatureWorker](./src/main/java/temps/TemperatureWorker.java). Luokka perii `Thread`-luokan. Kenttä `checked` on valmiina: se on `volatile int`, ja sitä kasvatetaan yhdellä jokaista käsiteltyä havaintoa kohti.

`run` lukee lähteestä `TemperatureSource.next()` kunnes lähde palauttaa tyhjän `OptionalDouble`-arvon. Jokainen lukema lisätään summaan ennen kuin `checked` kasvaa. Metodissa ei saa tulostaa eikä nukkua.

`average` palauttaa käsiteltyjen lukemien aritmeettisen keskiarvon. Jos yhtään lukemaa ei ole käsitelty, tulos on `0`. Keskiarvo luetaan luotettavasti vasta `join()`-kutsun jälkeen.

Arvioinnissa ajetaan testiluokka [TemperatureWorkerTest](./src/test/java/temps/TemperatureWorkerTest.java). Testit tarkistavat, että luokka on säie, että `checked` on `volatile`, että laskuri etenee havainto kerrallaan, että keskiarvo muuttuu uusien lukemien myötä, että kaksi säiettä on yhtä aikaa käynnissä ja että valmiin datan keskiarvot osuvat sallittuihin väleihin.


## Osa 3: pääohjelman silmukka *(edistynyt, 30 %)*

Toteuta `Main.runChart`. Se avaa molemmat CSV-tiedostot `TemperatureData.open`-metodilla, käynnistää säikeen `Las Palmas` ja säikeen `Helsinki`, piirtää ruutuja `showProgress`-metodilla niin kauan kuin jompikumpi säie on elossa ja nukkuu ruutujen välissä `Thread.sleep(redrawDelayMillis)`. Sen jälkeen se kutsuu `join()` kummallekin ja vasta sitten lukee `average()`-metodit ja palauttaa viimeisen ruudun.

Älä siirrä piirtämistä tai nukkumista työntekijöihin. Parametri `feedDelayMillis` on havaintojen syöttöviive; testit antavat sille arvoksi `0`.

Arvioinnissa ajetaan testiluokka [ParallelTempsTest](./src/test/java/temps/ParallelTempsTest.java). Se ajaa `runChart(0, 0)` ja tarkistaa viimeisen ruudun: molemmat palkit ovat 100 %, kaupungit on nimetty ja keskiarvot osuvat edellä kerrottuihin väleihin. Helsingin toleranssi on Las Palmasia väljempi.


## Lisenssi ja tekijät

Tämän tehtävän on laatinut Jukka Juslin kurssin aiempien tehtävärepositorioiden rakenteen pohjalta, ja se on lisensoitu [Creative Commons BY-NC-SA -lisenssillä](https://creativecommons.org/licenses/by-nc-sa/4.0/). Lämpötilat on haettu Open-Meteo Historical Weather API:sta, ja ne perustuvat ERA5-uusanalyysiin (Copernicus Climate Change Service / ECMWF). Open-Meteon lisenssi: [open-meteo.com/en/license](https://open-meteo.com/en/license).
