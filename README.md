# Jefferson Cylinder
Реализация шифра по принципу "Циллиндра Джефферсона". 
Шифратор состоит из цилиндра, образованного 36 деревянными дисками, которые вращались вокруг металлического вала. По краю каждого диска в разном порядке расположены 26 букв алфавита, так что, поворачивая диски, можно составить текст в любой строке.
После написания сообщения, выбирается любая другая строка букв на цилиндре и отправляется получателю.
Чтобы расшифровать сообщение, получатель, имеющий цилиндр с дисками, расположенными в том же порядке, должен был набрать на своем собственном цилиндре полученное закодированное сообщение в строку и найти строку с отправленным сообщением.
то-то, имеющий диски, мог перехватить сообщение, он не мог его расшифровать: важно то, в каком порядке эти диски расположены. Сколько способов их расположения существует? 
36! способов, то есть 371 993 326 789 901 217 467 999 448 150 835 200 000 000 способов расположения. ОЧЕНЬ трудно найти правильное положение дисков, если вы его не знаете!
