# Word distribution tool

## Opis sistema

- Input, Cruncher i Output
- razliciti tipovi inputa (neki koji citaju sa diska/weba)
- mozemo imati vise instanci
- svaka od komponenti treba da se izvrsava u zasebnoj niti
- input preko blokirajuceg reda
- cruncheri ce svaki zasebno imati svoj blokirajuci red
- output ima svoj blokirajuci red i cruncheri kada proizvedu rezultate salju u output
- jedan Thread Pool vezan za svaki tip komponente
- inputi 1 thread pool, cruncheri svoj i output svoj
- u pool-ovima se obavljaju poslovi
- po jedna nit za svaku instancu ona sluzi da cita podatke i prosledjuje dalje ako treba

## Input komponente

- svaki cruncher ima svoj input red (tu se salju podaci iz inputa kad procitaju)
- input su produceri, a cruncheri su consumeri
- jedan veliki thread pool koji je namenjen za sve file input komponente
- cim se nadje fajl za citanje, to je job za thread pool
- ako su datoteke na razlicitim diskovima, onda ih citamo konkurentno
- ako ih je vise na jednom disku, onda jedna po jedna
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