package org.duckdns.spacedock.lifepath;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.arch.lifecycle.ViewModelProviders;


/**
 * activité pour gérer le déroulement d'un lifepath. On doit utiliser AppCompat car c'est le meilleur moyen d'utiliser Toolbar.
 */
public class PathActivity extends AppCompatActivity
{
    /**
     * Il s'agit du ViewModel de l'activité : un objet persistant durant les changements d'orientation (détruit uniquement quand une application ne sera pas reconstruite, comme un retour au menu d'accueil par exemple). Cet objet est parfait pour faire le lien avewc la couche métier et sauvegarder des états pendants une phase de reconstruction (comme un changement d'orientation)
     */
    private PathModel m_model;
























    /**
     * activité tout juste créée : cette méthode sert surtout à initialiser les diverses variables et à peindre l'écran avec setContentView
     * NOTE IMPORTANTE : il n'y a dans cette activité pas besoin de la gestion dynamique des fragments : que l'on soit en portrait ou en paysage il y a un seul fragment identique.
     * On a donc simplement indiqué statiquement ce fragment dans le layout et il est inflaté automatiquement par setContentView : si on veut du dynamique il faut utiliser les FragmentManager.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //on peint l'écran
        super.onCreate(savedInstanceState);//le Bundle peut être fourni par l'OS, notamment quand l'activité redémarre après changement d'orientation écran













        setContentView(R.layout.activity_path);//inflate le layout : crée les objets vues et, dans le cas d'un objet composé comme ici, créée leur hiérarchie
        //setContentView ne peut être présent que dans une activité : car il permet de peindre un écran entier et de le rattacher au layout de plus haut niveau, les autres objets doivent inflater manuellement avec un inflater (les Fragments doivent le faire dans onCreateView par exemple)

        //ajout de la toolbar
        setSupportActionBar((Toolbar) findViewById(R.id.fad_toolbar));//l'actionbar par défaut étant désactivée par le thème il faut du coup indiquer programmatiquement quelle actionbar il faut utiliser : ici la toolbar définie dans le layout de l'activité
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//affiche la flèche de retour

        m_model = ViewModelProviders.of(this).get(PathModel.class);//le cycle de vue des ViewModel est géré automatiquement : cela raménera toujours le même objet (assurant la persistance de l'état), même quand l'activité est détruite et recréée, sauf si c'est définitif (retour au menu, pas simple changement d'orientation)
    }

    /**
     * Cette méthode sert surtout à indiquer quel fichier xml utiliser, le reste sera géré par la superclasse
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();//le super n'inflate pas automatiquement, il faut donc le faire avec un inflater dédié car ce n'est pas une vue lambda
        inflater.inflate(R.menu.lifepath_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Callback standard pour toutes les options, qu'elles soient dans le menu overflow, iconifiées dans la barre d'app ou même le bouton home/up
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)//home est traité comme back ("up") parce que onCreate le définit ainsi avec setDisplayHomeAsUpEnabled
        {
            if (!m_model.rollback())//à noter que le rollback est effectué dans le modèle car c'est lui le gardien de l'état des données, le fragment ne sert qu'à organiser l'affichage de son sous-écran
            {//on est au node initial, pour l'instant on affiche juste un message d'erreur, à terme on reviendra à l'activité de départ
                Snackbar.make(findViewById(R.id.lifepath_layout), R.string.rollback_impos, Snackbar.LENGTH_SHORT).show();//TODO traiter retour au menu, peut-être avec finish() et virer ce snack avec message en dur
                //TODO revenir a l'activité précédente
            }
        }

        //TODO autres boutons du menu

        return true; //important : consomme l'événement onClick, un false autorise la poursuite du traitement et d'autres éléments pourraient alors recevoir le onClick()
    }
}
