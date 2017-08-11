package edu.tigers.moduli;

import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.StartModuleException;
import org.apache.commons.configuration.SubnodeConfiguration;

import java.util.ArrayList;
import java.util.List;


/**
 * Structure for
 */
public abstract class AModule {
    private Class<? extends AModule> clazz;
    private String type;
    private SubnodeConfiguration subnodeConfiguration;
    private List<String> dependencies = new ArrayList<>();
    private boolean startModule = true;


    /**
     * Inits module.
     *
     * @throws InitModuleException if the module couldn't be initialized
     */
    public abstract void initModule() throws InitModuleException;


    /**
     * DeInits module.
     */
    public abstract void deinitModule();


    /**
     * Starts module.
     *
     * @throws StartModuleException if the module couldn't be started
     */
    public abstract void startModule() throws StartModuleException;


    /**
     * Stops module.
     */
    public abstract void stopModule();


    /**
     * @return the module clazz
     */
    public Class<? extends AModule> getId() {
        return clazz;
    }


    /**
     * @param clazz the module clazz
     */
    public void setId(final Class<? extends AModule> clazz) {
        this.clazz = clazz;
    }


    /**
     * @return the module type
     */
    public String getType() {
        return type;
    }


    /**
     * @param type the module type
     */
    public void setType(final String type) {
        this.type = type;
    }


    /**
     * @return the list of dependencies
     */
    public List<String> getDependencies() {
        return dependencies;
    }


    /**
     * @param dependencies the new list of dependencies
     */
    public void setDependencies(final List<String> dependencies) {
        this.dependencies = dependencies;
    }


    /**
     * @return the subnode configuration
     */
    public SubnodeConfiguration getSubnodeConfiguration() {
        return subnodeConfiguration;
    }


    void setSubnodeConfiguration(final SubnodeConfiguration subnodeConfiguration) {
        this.subnodeConfiguration = subnodeConfiguration;
    }


    @Override
    public String toString() {
        return clazz.getSimpleName();
    }


    /**
     * @return if the module should be started
     */
    public boolean isStartModule() {
        return startModule;
    }


    /**
     * @param startModule whether to start this module
     */
    public void setStartModule(final boolean startModule) {
        this.startModule = startModule;
    }
}
