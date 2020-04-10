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
- opcije za VM: -Xms3g -Xmx3g
- opcije za VM: -Xms500m -Xmx500m
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

# Facts

## Konkurentni alati

- synchronized je kao mutex, bitno je da kljuc bude nesto sto se ne menja u toku programa
- koristi se `.class` kao lock, ali je najbolje napraviti posebnu promenljivu koja ce biti public static da bude lock
- wait/notify moraju biti u kriticnoj sekciji (synchronized blok)
- semaphore se koristi kada imamo vise instanci nekon deljenog resursa, ali nije beskonacan (primer: stampac)
- CountDownLatch - kada imamo 2 kategorije niti, ove iz prvog dela su zavrsile prvi deo posla, a hoce da rade nesto drugo
pa moraju da sacekaju niti iz druge kategorije da zavrse svoj posao, pa moraju da cekaju (zovu await), a niti iz druge kategorije
kako zavrse posao zovu CountDownLatch i kada sve pozovu CountDownLatch i zavrse odbrojavanje, onda se iz prve kategorije odblokiraju i nastave dalje

## Thread pool

- Future - jako bitan
- FutureTask uzima Callable i kreira Thread objekat nad njim
- Executor sistem
- 01:23:06
- nema komunikacije izmedju niti
- submit() najbitnija metoda
- shutdown() obavezan - ceka da se zavrse sve niti i tek onda se gasi sve
- CompletionService bitan
- ForkJoinPool i ForkJoinTask dele taskove na pola - fork(), compute() i join()

## Cancellation i GUI

- poison pill sablon za prekidanje niti
- 50:00 - bitno
- JavaFX ima Task klasu koja ce da se koristi u konkurentnim izracunavanjima
- 01:23:00 progress bar primer
- Platform.runLater()

# HOW TO

## FileInput

- ForkJoinPool za FileInputWorkera koji ucitava tekst u rezultat
- ograniciti broj Threadova i koliko cemo fajlova citati zbog memorije
- FileInput komponenta dobija disk na kome su datoteke kao argument
- obilazak direktorijuma stavlja abs putanje datoteka u BlockingQueue
- Čitanje jedne datoteke treba da se obavlja kao zaseban posao unutar Thread Pool-a koji je namenjen za sve FileInput komponente
- ForkJoinPool prosledjujemo svim FileInput komponentama i ogranicimo broj threadova
- svaka FileInput komponenta koristi max broj threadova u tom pool-u

# PITANJA I STA JOS FALI

## 2 Opis sistema

### 2.1 Input komponente

> kako da konkurentno citamo fajlove sa razlicitih diskova?

> kako da ogranicim ovo? koji thread poll da se koristi?
> Ako imamo datoteke koje se nalaze na različitim diskovima, onda ih treba čitati konkurentno, ali u jednom trenutku se vrši samo jedno čitanje sa jednog diska.
> Dakle ni u jednom trenutku ne sme unutar ovog Thread Pool-a da bude aktivno više niti od broja diskova.

### 2.2 Cruncher komponente

> gde se salje outputu da je zapocet cruncher, posto trenutno saljem sa inputa ka output da je posao spreman za crunching

> Čim prebrojavanje za neki input objekat počne, treba svim output komponentama prijaviti da je taj posao započet, i omogućiti im da ga prikažu kao aktivnog.  

### 2.3 Output komponente

> Ova komponenta treba da obezbedi agregaciju već izračunatih rezultata, konkretno unijom i sumiranjem.  
> Output komponenti treba da bude moguće zadati posao koji će ovo da obavlja i taj posao treba obavljati unutar Thread Pool-a koji je namenjen za sve output komponente.  

> sta znaci unija ova? zar ne treba na single result da se dobije vrednost za taj fajl, a sum ce da sumira za sve selektovane?
> Sama agregacija se radi tako što se formira unija svih navedenih rezultata.  
> Ako u različitim rezultatima postoje brojevi pojavljivanja za jednu te istu reč ili vreću, onda treba sumirati te rezultate u uniji.  
> Posao treba da počne od praznog skupa i da čeka da rezultati postanu dostupni u proizvoljnom redosledu.  
> Nije potrebno dodavati rezultate u redosledu u kojem su postali dostupni.

> Za sve rezultate (pojedinačne i sumirane) treba obezbediti operacije poll() i take().  
> jel okej za poll() samo da proverim da li u listview naziv ima zvezdicu (*) ili ne?
> Operacija poll() je neblokirajuća operacija čitanja koja vraća null ako rezultat još uvek nije spreman.  

> Operacija take() treba da blokira nit koja ju je pozvala sve dok rezultati nisu spremni za čitanje.  
> Ako su rezultati spremni, operacija take() nije blokirajuća, već samo vraća rezultate.

## 3 Kvalitet sistema

> U slučaju da tokom rada ponestane RAM-a pri čitanju ili obradi, treba prijaviti grešku i odmah prekinuti rad čitavog programa, bez urednog gašenja svih poslova i niti.

> Ako se traže rezultati izračunavanja, a oni nisu spremni za prikaz, to treba prijaviti. - ovo je poll(), trenutno gledam samo zvezdicu ako ima i bacim alert

> Izlaz iz aplikacije treba da bude uredan, što podrazumeva zabranu započinjanja novih poslova, čekanje da se svi započeti poslovi završe, i gašenje svih niti bez nasilnog prekidanja.  
> file input i cruncher poslovi bacaju out of memory error i to catchujem, prikazujem alert i gasim sistem preko system.exit(0) - jel to okej?

## 4 GUI i podešavanje sistema

### 4.1 GUI

> kako da racunam vreme za progress bar?
> Rezultat treba sortirati opadajuće po broju pojavljivanja reči u posebnoj niti, čiji progres se prikazuje pomoću progress bar komponente.  
> Progres se osvežava na svakih K poređenja unutar procesa sortiranja, gde se K čita iz konfiguracione datoteke.  
> Pretpostaviti da će čitav proces imati N*logN poređenja ako se koristi Collections.sort().  

> Tokom sumiranja prikazati progres pomoću progress bar komponente koja se osvežava nakon svakog spajanja rezultata.  
> Ako je startovano više suma, prikazati progres za svaku od njih.

## Scenariji

### Scenario 1

> output - buttons podesi normalno ponasanje
> sum fali i sort

### Scenario 3

> suma dok se crunchuju

### Scenario 4

> ne valja remove dir

### Scenario 5

> -Xms500m -Xmx500m treba da obori app

> -Xms3g -Xmx3g treba da radi

### Random

> idle label ima mali delay kada zavrsi sa citanjem, pa se tek onda promeni

> gasenje aplikacije da se sredi i dok radi input - kako da se resi gasenje?
> jel moze preko shutdown(), pa awaitTermination()
> i kako da prestanem nakon toga da ubacujem fajlove u crunchera?

> CruncherMiddleware (85) - bug pause/start za crunchera i brisanje procitanih fajlova

> pauzirati threadove/while loops sve na on close