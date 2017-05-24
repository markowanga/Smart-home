# Smart-home
System domu inteligentnego - inteligentne sterowanie bramą i furtką za pomocą telefonu

System ma pomóc w zastąpieniu pilotów na fale radiowe do sterowania ogrodzeniem domu (bramą automatyczną oraz furtką) na rzecz telefonów z Androidem.

System składa się z 3 elementów: 
* serwera - który zarządza wszystki, odbiera wysyła identyfikuje, oraz zapisuje sygnały, jest napisany w node.JS oraz docelowo jest umieszczony na heroku
* mq - urządzenie współpracujące ze sterownikiem bramy, wybór padł na ESP8266-12F, ponieważ kontroller ma bardzo dobry nadajnik Wi-Fi, oraz można go programować poprzez Arduino IDE co bardzo wygodne, ponieważ można wykorzystać wiele ciekawych bibliotek
* telefonów z Androidem - urządzenie do interkacji z użytkownikiem, to za jego pomocą otwiera się brama oraz furtka, nie trzeba nosić żadnych dodatkowych urządzeń

## Automatyczne otwieranie bramy dzięki geolokalizacji
Bardzo ciekawa funkcjonalność która jest możliwa kiedy telefon steruje bramą. Cały system wygląda następująco:
* telefon ma zapisane 2 punkty z każdej możliwej drogi którą można przyjechać do domu
* w tle cały czas co 5 sekund jest sprawdzana aktywność telefonu - czy telefon jest w spoczynku, jeździ rowerem, sammochodem itp.
* kiedy zostanie wykryty ruch autem włącza się sprawdzanie lokallizacji - również co 5 sekund - jeślli telefon jest daleko od domu to położenie jest ustalane na podstawie sieci komórkowej, natomiast dopiero w bliskich odległościach włącza się GPS - pozwala to bardzo zaoszczędzić baterię
* brama wjazdowa do domu otworzy się kiedy przez 2 ustalone punkty przejedzie samochód w odpowiednim czasie

Dzięki zastosowaniu różnych dokładności lokalizacji aplikacja nie nadwyręża zbytnio baterii - przy normalnm korzystaniu z telefonu android pokazuje dla aplikacji zużyciue baterii na poziomie 3%.

## Techniczna strona systemu - elementy systemu łączą się ze sobą
Telefon kiedy ma za zadanie wysłać informację posługuje się zapytaniem HTTPS w którym podaje m.in. swoje IMEI dzięki czemu system jest w stanie odrzucić zgłoszenia z zewnątrz - np kiedy ktoś zainstaluje naszą apliakcję na swoim telefonie

W mikrokontolerze już nie mogło zostać wykorzystane t rozwiązanie, ponieważ tutaj serwer musi wysyłać dane do mikrokontrolera. Należało więc zastosować websocket. 
