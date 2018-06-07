package org.duckdns.spacedock.lifepath;

import android.arch.lifecycle.ViewModel;
import android.content.res.Resources;
import android.view.View;

import org.duckdns.spacedock.liblifepath.PathNavigator;

/**
 * Il s'agit du ViewModel de l'activité Path : un objet persistant durant les changements d'orientation (détruit uniquement quand une application ne sera pas reconstruite, comme un retour au menu d'accueil par exemple). Cet objet est parfait pour faire le lien avec la couche métier et sauvegarder des états pendants une phase de reconstruction (comme un changement d'orientation)
 * Tout le traitement des données doit se faire ici (c'est pour cela que c'est lui qui possède l'adapter) car l'activité ne fait qu'organiser les divers sous-écrans et le fragment ne fait que gérer l'affichage de sa partie
 */
public class PathModel extends ViewModel implements PathUIFragment.DataInterface
{
    /**
     * le navigateur, principal objet métier, le PathModel joue le rôle d'un DAO dans ses rapports avec lui
     */
    private final PathNavigator m_navigator = new PathNavigator();

    /**
     * le gestionnaire des éléments devant être affichés dans la RecyclerView du fragment, gardé ici pour deux raisons :
     * 1) la persistance, cet objet étant conservé lors des recréations d'activité
     * 2) l'encapsulation : tous les accès métiers étant gérés dans cette classe sans polluer l'UI
     */
    private PathAdapter m_adapter;


    /**
     * On avance dans l'arbre des choix, en maintenant la synchronicité entre l'adapter et le navigateur
     * @param p_id
     */
    public void decide(String p_id)
    {
        if(p_id != null)
        {
            m_adapter.takeDecision(m_navigator.decide(p_id));
        }
        else
        {
            throw new NullPointerException(Resources.getSystem().getString(R.string.error_nullId));//on utilise ici l'appel à Resources pour accès à R en dehors de l'activité
        }
    }

    /**
     * fait un retour dans l'arbre des choix
     * @return true si le retour était possible, false sinon
     */
    boolean rollback()
    {
        if (m_navigator.canRollback())
        {//Le navigateur accepte de reculer
            m_navigator.rollback();
            m_adapter.rollBack();
            return true;
        }
        else
        {//impossible de revenir en arrière
            return false;
        }
    }

    /**
     *
     * @return la dernière position accessible dans l'adapter : ceci permet par exemple de scroller la RecyclerView jusqu'au bout
     */
    public int getLastPos()
    {
        return m_adapter.getItemCount()-1;
    }

    /**
     * Cette méthode contrôle l'accès à l'adapter en forçant la mise à jour de l'inscription du listener, ainsi on ne fait jamais de callback sur un fragment détruit
     * @param p_listener
     * @return
     */
    public PathAdapter getPathAdapter(View.OnClickListener p_listener)
    {
        if (m_adapter == null)
        {//première invocation : on crée
            m_adapter = new PathAdapter(m_navigator.getCurrentChoice(), p_listener);
        } else
        {//invocation suivante, on renvoie juste après mise à jour du listener
            m_adapter.setListener(p_listener);
        }
        return m_adapter;
    }


}