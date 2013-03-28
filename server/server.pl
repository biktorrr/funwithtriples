:- module(server,
	  [ server/1,
	    setup/1
	  ]).
:- use_module(library(http/thread_httpd)).
:- use_module(library(http/http_dispatch)).
:- use_module(library(http/http_error)).
:- use_module(library(http/http_parameters)).
:- use_module(library(http/http_json)).
:- use_module(library(http/http_server_files)).
:- use_module(library(debug)).

:- use_module('zebra.chr').

server(Port) :-
	http_server(http_dispatch,
		    [ port(Port)
		    ]).

user:file_search_path(res, '/home/jan/Dropbox/SemWebOuting/fwt_assets').

:- http_handler(root(pair), pair, []).
:- http_handler(root(welcome), welcome, []).
:- http_handler(root(.), serve_files_in_directory(res), [prefix]).

pair(Request) :-
	http_parameters(Request,
			[ me(Me, []),
			  with(With, [])
			]),
	debug(fun, 'Pairing ~w with ~w', [Me, With]),
	pair(Me, With, World),
	world_json(World, JSON),
	reply_json(JSON).

welcome(Request) :-
	http_parameters(Request,
			[ me(Me, [])
			]),
	debug(fun, 'Welcome ~w', [Me]),
	welcome(Me, World),
	world_json(World, JSON),
	reply_json(JSON).

%%	world_json(+World, -JSON)

world_json(World, JSON) :-
	maplist(house_json, World, Houses),
	[ House1, House2, House3, House4, House5 ] = Houses,
	JSON = json([ house1=House1,
		      house2=House2,
		      house3=House3,
		      house4=House4,
		      house5=House5
		    ]).

house_json([ Color, Person, Car, Drink, Pet ],
	   json([ car=JCar,
		  color=JColor,
		  drink=JDrink,
		  person=JPerson,
		  pet=JPet
		])) :-
	jobj(Color, JColor),
	jobj(Person, JPerson),
	jobj(Car, JCar),
	jobj(Drink, JDrink),
	jobj(Pet, JPet).

jobj(Obj, JObj) :-
	(   var(Obj)
	->  JObj = @(null)
	;   JObj = Obj
	).

		 /*******************************
		 *	      WORLD		*
		 *******************************/

:- dynamic
	player_count/1,				% Count
	player/2,				% Int, Id
	constraints/2,				% Player, Constraints
	paired/3.				% Int1, Int2, Time

clean :-
	retractall(player_count(_)),
	retractall(player(_,_)),
	retractall(constraints(_,_)),
	retractall(paired(_,_,_)).

player_count(5).

setup(N) :-
	clean,
	assert(player_count(N)),
	handout(Decks),
	assert_constraints(Decks, 1).

assert_constraints([], _).
assert_constraints([Deck|T], I) :-
	assert(constraints(I, Deck)),
	I2 is I+1,
	assert_constraints(T, I2).

handout(Decks) :-
	player_count(Count),
	zebra:clues(All),
	random_permutation(All, Random),
	length(Decks, Count),
	deal(Random, 1, Count, Decks),
	maplist(close_list, Decks).

close_list(List) :-
	append(List, [], _), !.

deal([], _, _, _).
deal([H|T], I, Set, Decks) :-
	nth1(I, Decks, Deck),
	memberchk(H, Deck),
	I2 is I+1,
	(   I2 > Set
	->  In = 1
	;   In = I2
	),
	deal(T, In, Set, Decks).


assign_player(Id, Num) :-
	player(Num, Id), !.
assign_player(Id, Num) :-
	player_count(Count),
	between(1, Count, Num),
	\+ player(Num, _),
	assert(player(Num, Id)), !,
	debug(fun, 'Assigned ~q to ~q', [Num, Id]).

%%	welcome(Me, World)

welcome(Me, World) :-
	assign_player(Me, Num),
	constraints(Num, Clues),
	common(Clues, World).

%%	pair(Me, With, World)

pair(Me, With, World) :-
	assign_player(Me, X),
	assign_player(With, Y),
	get_time(Now),
	asserta(paired(X,Y,Now)),
	world(X, World).

world(Id, World) :-
	findall(I, paired(Id, I, _), IL),
	maplist(constraints, [Id|IL], List),
	append(List, Flat),
	sort(Flat, Unique),
	common(Unique, World).

