SCENARIOS :
client2 4

V3 12 essence client3 8, paces restes : 4
V1 14 diesel client1 9, places restes : 5
V2 4 diesel

Voici les nouvelles regles et fonctionnalites : ASSIGNATION VEHICULE :
On stocke la planification dans une table

On assigne les clients au vehicule par ordre decroissant du nb passagers : client1 4 passagers, client2 6 passagers, client3 2 passagers, on assigne le client2 en premier, puis client1 et finalement client3.

*Priorite des regles de gestion :
-NB PLACES >= NB PASSAGERS
-REMPLIR VEHICULE : si nb places restant >= nb passagers (on ne regarde pas encore nb places plus proche de nb passagers, on ne regarde pas nb de trajet, on ne regarde pas carburant, on remplit)
-NB PLACES LE PLUS PROCHE DU NB PASSAGERS
-VEHICULE AVEC LE MOINS DE TRAJET : trajet de V1 : aeorport > hotetl1 > hotel2 > aeroport, trajet V2 : aeroport > hotel1 > aeroport, alors c'est V2 qu'on choisit
-CARBURANT : diesel > essence > hybride > electrique

*Disponibilite :
CONSIDERATION DE L'HEURE D'ARRIVEE a l'aeroport du vehicule (le vehicule est disponible), MAIS CONSIDERATION DE TEMPS ATTENTE : debut temps attente : heure date_arrivee de la premiere reservation d'une date, puis premiere reservation apres le dernier temps d'attente, ainsi de suite ...

Ex : temps attente : 30 minutes, premiere resa de 12/03/2026 client1 : 07:15, premier temps attente : 07:15-07:45 donc REGROUPEMENT : client1 07:15, client2 07:25, client3 07:30, client4 07:40, client5 07:45, client1 et client4 n'ont pas trouve de vehicule, client2 va dans V1 et client3 et client4 va dans V2, le depart des 2 vehicules est a 07:40 (le dernier client assigne pendant ce temps d'attente). Puis client1 et client5 attendent la prochaine reservation pour le nouveau temps d'attente meme si il y a une vehocule qui retourne et arrive a l'aeroport ou est a l'aeroport avant cette prochaine reservation, si V1 retourne a 8:30 et que meme si ce vehicule est adequat a client1 ou client4 et que la prochaine reservation est a 9:20, client1 et client4 doivent attendre le nouveau temps d'attente 9:20-9:50. Tout ca en respectant les regles de gestions.