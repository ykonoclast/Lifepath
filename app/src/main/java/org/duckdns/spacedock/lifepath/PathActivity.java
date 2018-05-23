package org.duckdns.spacedock.lifepath;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.duckdns.spacedock.liblifepath.PathNavigator;

/**
 * activité pour gérer le ðeroulement d'un lifepath. On doit utiliser AppCompat car c'est le meilleur moyen d'utiliser Toolbar.
 */
public class PathActivity extends AppCompatActivity//TODO expliquer necessaire pour ToolBar, difference avec actionbar et besoin de mettre style dans manifeste et autre actions du tuto
{

    /**
     * l'adapter qui va servir à peupler les données dans la RecyclerView
     */
    private PathAdapter m_adapter;

    /**
     * navigateur de LifePath associé à l'activité, issu d'une librairie externe
     */
    private final PathNavigator m_navigator = new PathNavigator();

    /**
     * la vue scrollante qui va accueillir les textes et boutons
     */
    private RecyclerView m_recyclerView;

    /**
     * listener employé pour les boutons de navigations
     */
    private final View.OnClickListener m_navButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View p_view)
        {
            pathNavButtonCallback((String)p_view.getTag());//on se contente d'appeler une callback qui fera le tri
        }
    };

    /**
     * activité juste créée : cette méthode sert surtout à initialiser les diverses variables
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);//le Bundle peut être fourni par l'OS, notamment quand l'activité redémarre après changement d'orientation écran
        setContentView(R.layout.activity_path);//inflate le layout : crée les objets vues et, dans le cas d'un objet composé comme ici, créée leur hiérarchie

        setSupportActionBar((Toolbar)findViewById(R.id.fad_toolbar));//l'actionbar par défaut étant désactivée par le thème il faut du coup indiquer programmatiquement quelle actionbar il faut utiliser : ici la toolbar définie dans le layout de l'activité
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//affiche la flèche de retour

        m_adapter = new PathAdapter(m_navigator.getCurrentChoice(), m_navButtonListener);//c'est l'adapter qui passera ce listener aux boutons quand il les créera

        m_recyclerView = findViewById(R.id.recyclerview);
        m_recyclerView.setLayoutManager(new LinearLayoutManager(this));//on passe l'activité comme contexte : les thèmes passeront du coup et il n'y aura pas de fuite mémoire car les références sur la recyclerview sont dans l'activité, tout sera garbage-collecté ensemble
        m_recyclerView.setAdapter(m_adapter);
    }

    /**
     * Cette méthode sert surtout à indiquer quel fichier xml utiliser, le reste sera géré par la superclasse
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();//le super n'inflate pas automatiquement, il faut donc le faire avec un inflater dédié car ce n'est pas une vue lambda
        inflater.inflate(R.menu.lifepath_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Callback pour toutes les options, qu'elles soient dans le menu overflow, iconifiées dans la barre d'app ou même le bouton home/up
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)//home est traité comme back ("up") parce que oncreate le définit ainsi avec setDisplayHomeAsUpEnabled
        {
            if(m_navigator.canRollback())
            {//On n'est pas au node initial, on rollback la partie nav puis la partie graphique
                m_navigator.rollback();
                m_adapter.rollBack();
            }
            else
            {//on est au node initial, pour l'instant on affiche juste un message d'erreur, à terme on reviendra à l'activité de départ
                Snackbar.make(findViewById(R.id.lifepath_layout), R.string.rollback_impos,Snackbar.LENGTH_SHORT).show();//TODO traiter retour au menu, peut-être avec finish() et virer ce snack avec message en dur
            }
        }

        //TODO autres boutons du menu

        return true; //important : consomme l'événement onClick, un false autorise la poursuite du traitement et d'autres éléments pourraient alors recevoir le onClick()
    }

    /**
     * Callback pour la sélection de choix de Lifepath
     * @param p_tag
     */
    void pathNavButtonCallback(String p_tag)
    {
        m_adapter.takeDecision(m_navigator.decide(p_tag));//on active la navigateur pour qu'il fournisse le nouveau choix et on en informe l'adapter pour qu'il ajuste la RecyclerView
    }
}
