import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Redirect, Route, withRouter } from 'react-router-dom';
import Navigation from 'Navigation';
import Audit from 'component/audit/Index';
import AboutInfo from 'component/AboutInfo';
import DistributionConfiguration from 'distribution/Index';
import LogoutConfirmation from 'component/common/LogoutConfirmation';
import * as DescriptorUtilities from 'util/descriptorUtilities';
import GlobalConfiguration from 'dynamic/GlobalConfiguration';


class MainPage extends Component {
    constructor(props) {
        super(props);
        this.createRoutesForDescriptors = this.createRoutesForDescriptors.bind(this);
    }

    createRoutesForDescriptors(descriptorType, context, uriPrefix) {
        const { descriptors } = this.props;
        if (!descriptors) {
            return null;
        }
        const descriptorList = DescriptorUtilities.findDescriptorByTypeAndContext(descriptors, descriptorType, context);

        if (!descriptorList || descriptorList.length === 0) {
            return null;
        }
        const routeList = descriptorList.map(component => <Route key={component.urlName} path={`${uriPrefix}${component.urlName}`} render={() => <GlobalConfiguration key={component.name} descriptor={component} />} />);
        return routeList;
    }

    render() {
        const channels = this.createRoutesForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.CHANNEL, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/channels/');
        const providers = this.createRoutesForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.PROVIDER, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/providers/');
        const components = this.createRoutesForDescriptors(DescriptorUtilities.DESCRIPTOR_TYPE.COMPONENT, DescriptorUtilities.CONTEXT_TYPE.GLOBAL, '/alert/components/');
        return (
            <div>
                <Navigation />
                <div className="contentArea">
                    {providers && <Route
                        exact
                        path="/alert/"
                        render={() => (
                            <Redirect to={`${providers[0].props.path}`} />
                        )}
                    />}
                    {providers}
                    {channels}
                    <Route path="/alert/jobs/distribution" component={DistributionConfiguration} />
                    {components}
                    <Route path="/alert/general/audit" component={Audit} />
                    <Route path="/alert/general/about" component={AboutInfo} />
                </div>
                <div className="modalsArea">
                    <LogoutConfirmation />
                </div>
            </div>);
    }
}

MainPage.propTypes = {
    descriptors: PropTypes.arrayOf(PropTypes.object).isRequired
};
const mapStateToProps = state => ({
    descriptors: state.descriptors.items
});

export default withRouter(connect(mapStateToProps, null)(MainPage));
