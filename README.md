# Word distribution tool

## Pregled zadatka

- treba da bude napisan da radi kao pipeline
- konkurentno citanje, obradu i prikaz rezultata

## Opis sistema

- Input, Cruncher i Output - 02:57
- razliciti tipovi inputa (neki koji citaju sa diska/weba)
- mozemo imati vise instanci
- svaka od komponenti treba da se izvrsava u zasebnoj niti
- input preko blokirajuceg reda
- cruncheri ce svaki zasebno imati svoj blokirajuci red
- output ima svoj blokirajuci red i cruncheri kada proizvedu rezultate salju u output
- jedan Thread Pool vezan za svaki tip komponente
- inputi 1 thread pool, cruncheri svoj i output svoj - 04:41 
- u pool-ovima se obavljaju poslovi
- po jedna nit za svaku instancu ona sluzi da cita podatke i prosledjuje dalje ako treba

## Input komponente

- svaki cruncher ima svoj input red (tu se salju podaci iz inputa kad procitaju)
- input su produceri, a cruncheri su consumeri
- jedan veliki thread pool koji je namenjen za sve file input komponente
- cim se nadje fajl za citanje, to je job za thread pool
- ako su datoteke na razlicitim diskovima, onda ih citamo konkurentno
- ako je na jednom disku, onda jedna po jedna
- thread pool koji je zajednicki za sve fajlove treba da ima jednu nit koja je posvecena jednom disku
- jedna file input komponenta radi sa jednim diskom
- vise niti vise diskova
- input komponenta ima crunchere desno od sebe
- zapakuje jedan objekat u kome su ime datoteke i string - sadrzaj fajla
- u runtime-u razvezujemo inpute i crunchere
- ako cruncher hoce neki podatak, mora da se poveze pre nego sto pocne citanje
- 11:45 kaze to je feature (ako nije bug onda je feature)
- ostalo pise u specifikaciji

## Cruncher komponente

- ima input i output redove
- pise u tekstu sve i za bag of words i kako to treba

## Output komponente

- CacheOutput komponente cuva u memoriji rezultate (mapa)
- treba da obezbedi agregaciju vec izracunatih rezultata (unija i sumiranje)
- poll() i take() operacije

## Kvalitet sistema

- memorijska stabilnost i funkcionalni kvalitet
- opcije za VM -Xms3g -Xmx3g
- funkcionalni kvalitet je error handling i smislenost flowa kroz app

## GUI

- JavaFX
- ListView
- na GUI-ju se pokrece pozadinska nit koja sortira rezultate opadajuce
- ostalo pise

## Konfiguraciona datoteka

- citati preko Properties klase

  
# Pitanja

> Da li je ok da FileInput bude fixedThreadPool zbog maksimalno dve fileInput komponente?

> Da li je ok odvojiti fileInputComponentu i fileInputWorkera?

> Da li moze da se koristi Guava ili mora tekst da se obradjuje tako sto datoteku podelimo na delove i to dodelimo odredjenom broju niti iz threadPoola?

> Da li moze da se odvoji CrucherComponenta od CruncherWorkera i da li on moze da radi po principu deljenja datoteke da delove i dodeljivanje thread-ovima iz threadPoola?

> Ako moze da se odvoji, da li onda za svaku cruncher komponentu imamo pool za cruncher workere?

> Ako svaki cruncher worker ima svoj blocking queue i ako se na njega dodaje file, kako odredjujemo na koji blocking queue dodajemo datoteku ako ima vise crunchera ili se to gleda kao da saljemo na oba posto imaju razlicite arnosti?

> Da li postoji samo jedna Output komponenta na koju svi cruncheri stavljaju obradjene datoteke, posto u tekstu pise svaka cruncher komponenta je vezana za proizvoljno mnogo outPut blokirajucih redova nad kojima je producer? (dok je objasnjavao pricao je kako postoji jedna output komponenta za sve crunchere, zato ovo pitanje)
