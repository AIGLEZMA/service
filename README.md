# Service

Service est une application de bureau qui permet de structurer des données et de générer des documents PDF personnalisés à partir de modèles visuels.

## Fonctionnalités

- Créer des modèles de données adaptés à différents besoins : clients, installations, produits, interventions, etc.
- Ajouter des champs texte, nombre, décimal, référence ou liste.
- Enregistrer, consulter et archiver les données associées à chaque modèle.
- Importer des données depuis un fichier CSV avec association automatique des colonnes et validation avant import.
- Concevoir des modèles PDF avec un éditeur visuel.
- Ajouter du texte, des images, des formes, des listes, des tableaux, des QR codes et des codes-barres.
- Lier les éléments d'un document aux données enregistrées.
- Déplacer, redimensionner, aligner et organiser les éléments avec une grille et des repères intelligents.
- Prévisualiser un document avec les données sélectionnées avant de l'exporter.
- Générer et enregistrer le document final au format PDF.
- Annuler ou rétablir les modifications dans l'éditeur.

Les données et les modèles sont enregistrés localement dans une base SQLite.

## Utilisation

1. Créer un modèle de données et définir ses champs.
2. Ajouter les données qui seront utilisées dans les documents.
3. Créer un modèle PDF dans l'éditeur visuel.
4. Associer les champs, sélectionner les données et générer le PDF.

## Lancer l'application

```bash
./gradlew :desktopApp:run
```

## Vérification

```bash
./gradlew check
```

## À venir

- Prise en charge d'autres sources de données.
