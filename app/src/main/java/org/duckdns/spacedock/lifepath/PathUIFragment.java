package org.duckdns.spacedock.lifepath;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


/**
 * le fragment constituant l'écran principal de l'application
 * A noter qu'il n'a pas besoin de paramétres car il accède directement au ViewModel ce qui est bien plus propre.
 */
public class PathUIFragment extends Fragment
{
    /**
     * le ViewModel persistant contenant la couche d'accès aux données
     */
    @NonNull private DataInterface m_data;

    /**
     * la vue scrollante qui va accueillir les textes et boutons
     */
    @NonNull private RecyclerView m_recyclerView;

    /**
     * listener employé pour les boutons de navigations
     */
    private final View.OnClickListener m_navButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View p_view)
        {
            pathNavButtonCallback((String)p_view.getTag());//on utilise le Tag : un élément permettant d'encapsuler de la donnée d'état dans une vue, ici une chaîne de navigation
        }
    };

    /**
     * constructeur vide requis par la spécification
     */
    public PathUIFragment()
    {

    }

    /**
     * Cette méthode permet les initialisations graphiques, donc le travail sur les vues mais ATTENTION : l'activité n'est pas encore totalement créée (on peut accéder à ses objets associés comme le ViewModel mais sans doute pas à ses vraies méthodes ou champs)
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //pas de setContentView hors d'une activité : on produit donc la hiérarchie des vues manuellement avec un inflater
        FrameLayout layout = (FrameLayout) inflater.inflate(R.layout.fragment_ui_path, container, false);//On n'attache au root (le second paramétre) car ce sera fait ensuite par le code appelant cette méthode, si on met attachToRoot à true, une erreur se produira plus tard ("déjà attaché")

        //On peut affecter les variables de vues dans cette méthode, la stack graphique est prête
        m_recyclerView = layout.findViewById(R.id.recyclerview);
        m_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));//on passe l'activité comme contexte : les thèmes passeront du coup et il n'y aura pas de fuite mémoire car les références sur la recyclerview sont dans l'activité, tout sera garbage-collecté ensemble

        return layout;//TODO doc
    }

    /**
     * l'activité est ici totalement créée avec certitude, on peut donc y accéder sans crainte
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        m_data = ViewModelProviders.of(getActivity()).get(PathModel.class);//comme le provider assure l'unicité du ViewModel on récupère bien celui employé par l'activité.
        m_recyclerView.setAdapter(m_data.getPathAdapter(m_navButtonListener));//la recyclerview est accessible car on l'associe à ce champ dans onCreateView
    }

    /**
     * ici l'on est sûr que tout est bien démarré, on va donc scroller la RecyclerView à cet endroit en toute sécurité
     */
    @Override
    public void onStart()
    {
        super.onStart();
        m_recyclerView.scrollToPosition(m_data.getLastPos());//on scrolle vers le bas de la recyclerview afin de rester lisible sur changement d'orientation
    }

    /**
     * CRUCIAL : fuite mémoire sans cela car la RecyclerView garde reste en observer de l'adapter qui la leake alors avec tout le contexte
     */
    @Override
    public void onDestroyView()
    {
        m_recyclerView.setAdapter(null);
        super.onDestroyView();
    }

    /**
     * Callback pour la sélection de choix de Lifepath
     * @param p_tag
     */
    private void pathNavButtonCallback(String p_tag)
    {
        m_data.decide(p_tag);//on active la navigateur pour qu'il fournisse le nouveau choix et on en informe l'adapter pour qu'il ajuste la RecyclerView
        m_recyclerView.scrollToPosition(m_data.getLastPos());//on scrolle vers le bas de la recyclerview afin de rester lisible sur changement d'orientation
    }


    /**
     * interface standardisant l'interaction avec ce fragment : ici c'est un ViewModel qui l'implémente, ce pourrait aussi être l'activité (en ce cas un test sur la classe devrait être fait dans onAttach() pour vérifier que cette interface est vbien implémentée
     * L'emploi d'une interace permet de changer les composants ultérieurement : on pourrait passer les callbacks dans l'activité par exemple
     */
    public interface DataInterface
    {
        /**
         * callback pour la navigation
         * @param id
         */
        void decide(String id);

        /**
         * fournit un adapter et permet l'inscription du fragment aux boutons de navigations via le listner
         * @param listener
         * @return
         */
        PathAdapter getPathAdapter(View.OnClickListener listener);

        /**
         *
         * @return la dernière position accessible dans l'adapter : ceci permet par exemple de scroller la RecyclerView jusqu'au bout
         */
        int getLastPos();
    }
}
