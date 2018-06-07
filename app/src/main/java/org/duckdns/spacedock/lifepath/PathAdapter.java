package org.duckdns.spacedock.lifepath;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.duckdns.spacedock.liblifepath.PathNavigator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * alimente la RecyclerView en éléments à afficher
 */
class PathAdapter extends RecyclerView.Adapter
{
    /**
     * liste des choix effectués plus le choix actuel
     */
    private final List<PathNavigator.LifepathChoice> m_listChoices = new ArrayList<>();

    /**
     * la liste des décisions possibles à un instant donné, mise à jour en fonction de l'avancée/recul dans les choix
     */
    private final List<Map.Entry<String, String>> m_listPossibleDecisions = new ArrayList<>();

    /**
     * le listener passé par l'activité et activant sa callback pour les boutons de navigation
     */
    private View.OnClickListener m_listener;

    /**
     * constructeur : alimente la RecyclerView avec le choix initial
     *
     * @param p_choixInitial
     * @param p_listener
     */
    PathAdapter(PathNavigator.LifepathChoice p_choixInitial, View.OnClickListener p_listener)
    {
        m_listener = p_listener;
        m_listChoices.add(p_choixInitial);
        updatePossibleDecisions();//on collecte les décisions initiales
        //setHasStableIds(true);//inutile ici mais conservé pour documentation : assigne un id spécifique à chaque élément qui ne peut pas changer, différent de la position dans la RecyclerView. La méthode overridable getItemId permet de l'assigner ensuite.
    }

    /**
     * le nombre d'éléments est la somme des champs textes et des boutons
     *
     * @return
     */
    @Override
    public int getItemCount()
    {
        return m_listChoices.size() + m_listPossibleDecisions.size();
    }

    /**
     * renvoie le type (au sens de la RecyclerView) d'un élément en fonction de sa position afin de déterminer quels viewholders peuvent être recyclés pour quels nouvequx éléments
     *
     * @param p_position
     * @return
     */
    @Override
    public int getItemViewType(int p_position)
    {
        if (p_position < m_listChoices.size())
        {//on est dans les textes
            return (R.id.nodeText);//on renvoie l'ID de la view générique du fichier de layout, c'est beaucoup mieux qu'un nombre arbitraire car porteur de sens
        }
        else
        {//on est dans les boutons car après le dernier élément de de la liste des choix
            return (R.id.nodeButton);//idem : id pas valeur arbitraire
        }
    }

    /**
     * appelé quand un viewholder va être créé, c'est à dire quand il n'y a pas assez de ViewHolders pour contenir tous les éléments affichés à l'écran, sinon des ViewHolders anciens sont recyclés. Ce qui importe ici est de bien choisir le type de ViewHolder en fonction de la vue qui va être créée
     * Il faut donc aussi créer la vue qui sera associée dans le holder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p_parent, int p_viewType)
    {
        RecyclerView.ViewHolder result;

        //Au lieu de créer des vues programmatiquement on les définit dans des xml séparés (plus facile pour gérer les divers attributs) et on les inflate pour créer des objets

        if (p_viewType == R.id.nodeText)
        {//texte
            TextView text = (TextView)LayoutInflater.from(p_parent.getContext()).inflate(R.layout.text_node, p_parent, false);//l'inflater produit des vues en fonction d'un layout XML
            result = new PathViewHolder(text);
        }
        else
        {//bouton
            Button button = (Button)LayoutInflater.from(p_parent.getContext()).inflate(R.layout.button_node, p_parent, false);//idem, l'inflater peut produire dynamiquement des vues
            result = new ButtonViewHolder(button);
        }
        return result;
    }

    /**
     * appelé quand un ViewHolder va être bindé à de la donnée : il vient d'être créé ou doit être recyclé, en tout cas les éléments ont été mis à jour ou un scroll effectué : il faut donc associer des données au holder
     *
     * @param p_holder
     * @param p_position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder p_holder, int p_position)
    {
        if (p_holder.getItemViewType() == R.id.nodeText)
        {//texte, on peut donc directement utiliser la position dans la liste idoine
            ((PathViewHolder) p_holder).setText(m_listChoices.get(p_position).desc);//on assigne le texte de description du node
        }
        else
        {//bouton on doit donc ajuster la position du nombre de champs textes la précédant
            int index = p_position - m_listChoices.size();
            Map.Entry<String, String> entry = m_listPossibleDecisions.get(index);
            ((ButtonViewHolder) p_holder).setElements(entry.getKey(), entry.getValue(), m_listener);//on assigne les descriptions et codes de navigation des boutons
        }
    }


    //méthodes custom de gestion des ajouts/retraits

    /**
     * méthode utilisée quand on avance dans l'arbre
     *
     * @param p_choice
     */
    void takeDecision(PathNavigator.LifepathChoice p_choice)
    {
        delButtons();
        m_listChoices.add(p_choice);
        notifyItemChanged(getItemCount());//un item est ajouté, on cible donc un élément au delà de l'actuel max
        addButtons();
    }

    /**
     * méthode utilisée pour reculer dans l'arbre
     */
    void rollBack()
    {
        delButtons();
        m_listChoices.remove(m_listChoices.size() - 1);
        notifyItemRemoved(getItemCount() - 1);//-1 car on veut supprimer : on cible donc bien le dernier élément
        addButtons();
    }

    /**
     * appelé pour inscrire un nouveau fragment aux événements sur les boutons de la RecyclerView associée à cet adapter (vraisemblablement quand un fragment est recréé après un changement d'orientation)
     *
     * @param p_listener
     */
    void setListener(View.OnClickListener p_listener)
    {
        m_listener = p_listener;
    }

    /**
     * supprime les boutons du dernier choix affiché
     */
    private void delButtons()
    {
        int formerItemCount = getItemCount();
        int toDelete = m_listPossibleDecisions.size();
        m_listPossibleDecisions.clear();
        notifyItemRangeRemoved(formerItemCount - toDelete - 1, toDelete);
    }



    /**
     * ajoute les boutons du choix actuel
     */
    private void addButtons()
    {
        PathNavigator.LifepathChoice currentChoice = m_listChoices.get(m_listChoices.size() - 1);
        int toAdd = currentChoice.decisionsPossibles.size();
        updatePossibleDecisions();
        notifyItemRangeChanged(getItemCount() - toAdd - 1, toAdd);//la liste est à ce moment là en avance sur la réalité de la RecyclerView, d'où le -1
    }

    /**
     * peuple la liste des décisions possibles avec celles associées au node actuel
     */
    private void updatePossibleDecisions()
    {
        if (m_listPossibleDecisions.isEmpty())
        {
            for (Map.Entry<String,String> entry : m_listChoices.get(m_listChoices.size() - 1).decisionsPossibles.entrySet())
            {
                m_listPossibleDecisions.add(entry);
            }
        }
        else
        {
            throw new IllegalStateException(Resources.getSystem().getString(R.string.error_decnonvide));//on utilise ici l'appel à Resources pour accès à R en dehors de l'activité
        }
    }

    /**
     * classe pour contenir une vue texte, superclasse de celle utilisée pour les boutons
     */
    static class PathViewHolder extends RecyclerView.ViewHolder
    {
        /**
         * constructeur, on utilise TextView comme classe de paramétre car les boutons en héritent (donc passeront) mais cela empêche de passer n'importe quoi (une seekbar par exemple)
         *
         * @param p_view
         */
        public PathViewHolder(TextView p_view)
        {
            super(p_view);
        }

        /**
         * met à jour le texte du champ, c'est forcément une TextView (ou une classe en héritant) car on le force au constructeur de cette classe
         *
         * @param p_text
         */
        void setText(String p_text)
        {
            ((TextView) itemView).setText(p_text);
        }
    }

    /**
     * classe pour contenir une vue bouton, peut étendre la PathViewHolder car les Button héritent des TextView
     */
    static class ButtonViewHolder extends PathViewHolder
    {
        /**
         * constructeur, on restreint ici aux Buttons, qui sont des TextView donc ça passera
         *
         * @param p_view
         */
        public ButtonViewHolder(Button p_view)
        {
            super(p_view);
        }

        /**
         * ajoute les éléments à associer au bouton
         *
         * @param p_tag      le code du node associé plus tard dans l'arbre
         * @param p_text     la description à afficher sur le bouton
         * @param p_listener le listner vers la callback de l'activité
         */
        void setElements(String p_tag, String p_text, View.OnClickListener p_listener)
        {
            setText(p_text);
            itemView.setTag(p_tag);//On utilise le système des Tags qui permettent d'associer un élément de donnée pouvant représenter un état à n'importe quelle vue, le bouton porte ainsi en plus de son label une information de navigation utilisée pour la suite
            itemView.setOnClickListener(p_listener);
        }
    }
}
