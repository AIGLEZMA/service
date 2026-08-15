# Fichiers de test CSV

Dans l'application, ouvrez le modèle de données **Test Import CSV**, puis cliquez sur **Importer CSV**.

| Fichier | Résultat attendu |
| --- | --- |
| `contacts_valides.csv` | 3 lignes importables, séparateur virgule détecté automatiquement. |
| `contacts_point_virgule.csv` | 2 lignes importables, séparateur point-virgule détecté automatiquement. |
| `contacts_tabulations.csv` | 2 lignes importables, séparateur tabulation détecté automatiquement. |
| `contacts_mapping_manuel.csv` | Associer manuellement Contact, Mail, Years, Rating, Groups et Client ID aux champs correspondants. |
| `contacts_erreurs_types.csv` | Import bloqué : Age et Score sont invalides à la ligne 2. |
| `contacts_entetes_dupliques.csv` | Fichier rejeté : les en-têtes Email et email sont considérés comme des doublons. |

Les valeurs `client_dupont` et `client_martin` correspondent aux clients de démonstration déjà présents dans l'application.
