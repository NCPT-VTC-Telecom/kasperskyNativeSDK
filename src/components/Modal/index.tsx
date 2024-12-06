import * as React from 'react';
import {Modal, Portal, Text, Button, PaperProvider} from 'react-native-paper';
import type {PropsWithChildren} from 'react';

const PopUpModal: React.FC<PropsWithChildren> = props => {
  const [visible, setVisible] = React.useState(false);

  const showModal = () => setVisible(true);
  const hideModal = () => setVisible(false);
  const containerStyle = {backgroundColor: 'white', padding: 20};

  return (
    <PaperProvider>
      <Portal>
        <Modal
          visible={visible}
          onDismiss={hideModal}
          contentContainerStyle={containerStyle}>
          {props.children}
        </Modal>
      </Portal>
    </PaperProvider>
  );
};

export default PopUpModal;
