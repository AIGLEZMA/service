# Fichiers de test CSV

Créez un modèle de données dédié, puis ajoutez les champs suivants avant de cliquer sur **Importer CSV** :

| Nom | Type |
| --- | --- |
| Full Name | Texte |
| Email | Texte |
| Age | Nombre entier |
| Score | Nombre décimal |
| Tags | Liste |
| Customer Reference | Texte |

| Fichier | Résultat attendu |
| --- | --- |
| `contacts_valides.csv` | 3 lignes importables, séparateur virgule détecté automatiquement. |
| `contacts_point_virgule.csv` | 2 lignes importables, séparateur point-virgule détecté automatiquement. |
| `contacts_tabulations.csv` | 2 lignes importables, séparateur tabulation détecté automatiquement. |
| `contacts_mapping_manuel.csv` | Associer manuellement Contact, Mail, Years, Rating, Groups et Client ID aux champs correspondants. |
| `contacts_erreurs_types.csv` | Import bloqué : Age et Score sont invalides à la ligne 2. |
| `contacts_entetes_dupliques.csv` | Fichier rejeté : les en-têtes Email et email sont considérés comme des doublons. |

Ces fichiers restent réservés aux tests manuels et automatisés ; ils ne sont plus injectés dans la base de production.
