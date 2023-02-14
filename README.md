Common options:

`./run.sh -input "<input directory>" -output "<output directory>"`

Mapping commit authors:

`./run.sh -input "<input directory>" -authors`

Mapping time:

`./run.sh -input "<input directory>" -time 2022-12-03T10:15:30 2023-12-03T10:15:30`

Squashing chains:

`./run.sh -input "<input directory>" -chains`

Picking longest commit path:

`./run.sh -input "<input directory>" -tree`

Repositories to play with:

`git clone https://github.com/janfornal/GameLoader.git`

`git clone https://github.com/mhorod/chess.git`

Pretty git log:

`git log --decorate --oneline --graph`
