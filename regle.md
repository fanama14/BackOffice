REGLES METIER - PLANIFICATION TRANSPORT

1. OBJECTIF
- Assigner les reservations a des vehicules selon des regles de priorite.
- Enregistrer le resultat dans la table de planification.

2. ORDRE DE TRAITEMENT DES CLIENTS
- Dans une fenetre de temps d'attente, les clients sont traites par ordre decroissant du nombre de passagers.
- Exemple: client1=4, client2=6, client3=2 -> ordre de traitement: client2, client1, client3.

3. PRIORITE DES REGLES D'ASSIGNATION
- Regle 1: NB_PLACES >= NB_PASSAGERS (contrainte obligatoire).
- Regle 2: REMPLIR VEHICULE.
	Si un vehicule deja ouvert dans la fenetre a assez de places restantes, on y met le client en priorite.
	Dans ce cas, on ne regarde pas encore la proximite de capacite, le nombre de trajets, ni le carburant.
- Regle 3: NB PLACES LE PLUS PROCHE DU NB PASSAGERS.
- Regle 4: VEHICULE AVEC LE MOINS DE TRAJETS.
	Exemple: V1 fait aeroport > hotel1 > hotel2 > aeroport, V2 fait aeroport > hotel1 > aeroport -> V2 est prioritaire.
- Regle 5: TYPE CARBURANT (priorite): diesel > essence > hybride > electrique.

4. DISPONIBILITE DES VEHICULES
- Un vehicule est disponible des qu'il est revenu a l'aeroport (heure de retour atteinte).
- La verification de disponibilite se fait par rapport a l'heure de depart potentielle du groupe
	(dernier client assigne dans la fenetre), et pas uniquement a l'heure d'ancrage de la fenetre.

5. GESTION DES FENETRES DE TEMPS D'ATTENTE
- Debut de la premiere fenetre: heure d'arrivee de la premiere reservation du jour.
- Duree de la fenetre: parametre temps_attente (ex: 30 minutes).
- Fenetre suivante: demarre a la premiere reservation strictement apres la fin de la fenetre precedente.
- Si des clients ne sont pas assignes dans une fenetre, ils attendent la fenetre suivante.
- Meme si un vehicule revient entre-temps, on ne cree pas de fenetre intermediaire sans nouvelle reservation ancre.

6. EXEMPLE DE FENETRE
- Temps attente = 30 min.
- Premiere reservation du 12/03/2026 a 07:15 -> fenetre 07:15-07:45.
- Le depart des vehicules assignes dans cette fenetre se fait a l'heure du dernier client assigne de la fenetre.
- Les clients non assignes attendent la fenetre suivante ancree par la prochaine reservation.

7. CAS IMPORTANT (IMAGE FOURNIE)
- Si VH-001 est deja utilise, puis revient a l'aeroport avant ou a l'heure de depart du groupe suivant,
	alors VH-001 peut etre reutilise.
- Donc les 3 dernieres reservations ne doivent pas rester en "Aucun vehicule disponible"
	si VH-001 est redevenu disponible au moment du depart reel de leur groupe.