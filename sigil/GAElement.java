package sigil;
/**
 * Interface that mutatable, breedable elements must
 * implement
 * 
 * @author John Williamson
 */

public interface GAElement
{
    /**
     * Mutate by the given factor.
     * 0.0 = no change, 1.0 = complete change (equivalent to reinitialization)
     */
    public void mutate(double mutFactor);

    /**
     * Set the current attributes to be those of
     * gaElt; only works if objects are of exactly
     * the same type. This is a shallow copy -- 
     * deep copies can be obtained with the copy()
     * method.
     */
    public void copyFrom(GAElement gaElt);


    /**
     * Breed two elements together in some implementation defined way
     * Note that two unrelated GAElements cannot breed succesfully,
     * just like in real life!
     */
    public void breed(GAElement toBreed);

    /**
     * Create a deep copy of this element and return it
     *
     * @return A deep copy of this element
     */
    public GAElement copy();

    /**
     * Do some implementation defined "preview" of the current
     * state of the element
     */
    public void preview();

}
